package org.apache.synapse.core.axis2;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sandesha2.client.SandeshaClientConstants;
import org.apache.synapse.FaultHandler;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.transport.nhttp.NhttpConstants;

import java.util.*;

/**
 * This is the message receiver that receives the responses for outgoing messages sent out
 * by Synapse. It holds a callbackStore that maps the [unique] messageID of each message to
 * a callback object that gets executed on timeout or when a response is recived (before timeout)
 *
 * The AnonymousServiceFactory uses this MessageReceiver for all Anonymous services created by it.
 * This however - effectively - is a singleton class
 */
public class SynapseCallbackReceiver implements MessageReceiver {

    private static final Log log = LogFactory.getLog(SynapseCallbackReceiver.class);

    /** This is the synchronized callbackStore that maps outgoing messageID's to callback objects */

    /**
     * Create the *single* instance of this class that would be used by all anonymous services
     * used for outgoing messaging.
     * @param synCfg the Synapse configuration
     */
    public SynapseCallbackReceiver(SynapseConfiguration synCfg) {

        callbackStore = Collections.synchronizedMap(new HashMap());

        TimeoutHandler timeoutHandler = new TimeoutHandler(callbackStore);
        
        Timer timeOutTimer = synCfg.getSynapseTimer();
        long timeoutHandlerInterval = SynapseConstants.DEFAULT_TIMEOUT_HANDLER_INTERVAL;
        try {
            timeoutHandlerInterval = Long.parseLong(
                System.getProperty(SynapseConstants.TIMEOUT_HANDLER_INTERVAL));
        } catch (Exception ignore) {}

        timeOutTimer.schedule(timeoutHandler, 0, timeoutHandlerInterval);
    }


    public void addCallback(String MsgID, AxisCallback callback) {
        callbackStore.put(MsgID, callback);
    }

    /**
     * Everytime a response message is received this method gets invoked. It will then select
     * the outgoing *Synapse* message context for the reply we received, and determine what action
     * to take at the Synapse level
     *
     * @param messageCtx the Axis2 message context of the reply received
     * @throws AxisFault
     */
    public void receive(MessageContext messageCtx) throws AxisFault {

        String messageID = null;

        if (messageCtx.getOptions() != null && messageCtx.getOptions().getRelatesTo() != null) {
            Options options = messageCtx.getOptions();
            if (options != null) {
                RelatesTo relatesTo = options.getRelatesTo();
                if (relatesTo != null) {
                    messageID = relatesTo.getValue();
                }
            }
        } else if (messageCtx.getProperty(SandeshaClientConstants.SEQUENCE_KEY) == null) {
            messageID = (String) messageCtx.getProperty(SynapseConstants.RELATES_TO_FOR_POX);
        }

        if (messageID != null) {
            AxisCallback callback = (AxisCallback) callbackStore.remove(messageID);

            RelatesTo[] relates = messageCtx.getRelationships();
            if (relates != null && relates.length > 1) {
                removeDuplicateRelatesTo(messageCtx, relates);
            }
            
            if (callback != null) {
                handleMessage(messageCtx, ((AsyncCallback) callback).getSynapseOutMsgCtx());
                
            } else {
                log.warn("Synapse received a response for the request with message Id : " +
                    messageID + " But a callback has not been registered to process this response");
            }

        } else if (!messageCtx.isPropertyTrue(NhttpConstants.SC_ACCEPTED)){
            log.warn("Synapse received a response message without a message Id");
        }
    }

    /**
     * Handle the response or error (during a failed send) message received for an outgoing request
     *
     * @param response         the Axis2 MessageContext that has been received and has to be handled
     * @param synapseOutMsgCtx the corresponding (outgoing) Synapse MessageContext for the above
     *                         Axis2 MC, that holds Synapse specific information such as the error
     *                         handler stack and local properties etc.
     * @throws AxisFault 
     */
    private void handleMessage(MessageContext response,
        org.apache.synapse.MessageContext synapseOutMsgCtx) throws AxisFault {

        Object o = response.getProperty(NhttpConstants.SENDING_FAULT);
        if (o != null && Boolean.TRUE.equals(o)) {


            Stack faultStack = synapseOutMsgCtx.getFaultStack();
            if (faultStack != null && !faultStack.isEmpty()) {
                SOAPEnvelope envelope = response.getEnvelope();
                if (envelope != null) {
                    SOAPFault fault = envelope.getBody().getFault();
                    if (fault != null) {
                        Exception e = fault.getException();
                        if (e == null) {
                            e = new Exception(fault.toString());
                        }
                        synapseOutMsgCtx.setProperty(SynapseConstants.ERROR_CODE,
                            SynapseConstants.SENDING_FAULT);
                        SOAPFaultReason faultReason = fault.getReason();
                        if (faultReason != null) {
                            synapseOutMsgCtx.setProperty(SynapseConstants.ERROR_MESSAGE,
                                    faultReason.getText());
                        }
                        ((FaultHandler) faultStack.pop()).handleFault(synapseOutMsgCtx, e);
                    }
                }
            }

        } else {

            Stack faultStack = synapseOutMsgCtx.getFaultStack();
            if (faultStack !=null && !faultStack.isEmpty()
                && faultStack.peek() instanceof Endpoint) {
                faultStack.pop();
            }
            if (log.isDebugEnabled()) {
                log.debug("Synapse received an asynchronous response message");
                log.debug("Received To: " +
                        (response.getTo() != null ? response.getTo().getAddress() : "null"));
                log.debug("SOAPAction: " +
                        (response.getSoapAction() != null ? response.getSoapAction() : "null"));
                log.debug("WSA-Action: " +
                        (response.getWSAAction() != null ? response.getWSAAction() : "null"));
                String[] cids = response.getAttachmentMap().getAllContentIDs();
                if (cids != null && cids.length > 0) {
                    for (int i = 0; i < cids.length; i++) {
                        log.debug("Attachment : " + cids[i]);
                    }
                }
                log.debug("Body : \n" + response.getEnvelope());
            }
            MessageContext axisOutMsgCtx =
                    ((Axis2MessageContext) synapseOutMsgCtx).getAxis2MessageContext();

            response.setServiceContext(null);
            response.setOperationContext(axisOutMsgCtx.getOperationContext());
            response.getAxisMessage().setParent(
                axisOutMsgCtx.getOperationContext().getAxisOperation());
            response.setAxisService(axisOutMsgCtx.getAxisService());

            response.setServerSide(true);
            response.setProperty(SynapseConstants.ISRESPONSE_PROPERTY, Boolean.TRUE);
            response.setProperty(MessageContext.TRANSPORT_OUT,
                    axisOutMsgCtx.getProperty(MessageContext.TRANSPORT_OUT));
            response.setProperty(org.apache.axis2.Constants.OUT_TRANSPORT_INFO,
                    axisOutMsgCtx.getProperty(org.apache.axis2.Constants.OUT_TRANSPORT_INFO));
            response.setTransportIn(axisOutMsgCtx.getTransportIn());
            response.setTransportOut(axisOutMsgCtx.getTransportOut());

            response.setDoingREST(axisOutMsgCtx.isDoingREST());
            if (axisOutMsgCtx.isDoingMTOM()) {
                response.setDoingMTOM(true);
                response.setProperty(
                        org.apache.axis2.Constants.Configuration.ENABLE_MTOM,
                        org.apache.axis2.Constants.VALUE_TRUE);
            }
            if (axisOutMsgCtx.isDoingSwA()) {
                response.setDoingSwA(true);
                response.setProperty(
                        org.apache.axis2.Constants.Configuration.ENABLE_SWA,
                        org.apache.axis2.Constants.VALUE_TRUE);
            }

            response.removeProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE);
            
            if(axisOutMsgCtx.isSOAP11() != response.isSOAP11()) {
            	if(axisOutMsgCtx.isSOAP11()) {
            		SOAPUtils.convertSOAP12toSOAP11(response);
            	} else {
            		SOAPUtils.convertSOAP11toSOAP12(response);
            	}
            }

            if (axisOutMsgCtx.getMessageID() != null) {
                response.setRelationships(
                        new RelatesTo[]{new RelatesTo(axisOutMsgCtx.getMessageID())});
            }

            Axis2MessageContext synapseInMessageContext =
                    new Axis2MessageContext(
                            response,
                            synapseOutMsgCtx.getConfiguration(),
                            synapseOutMsgCtx.getEnvironment());

            synapseInMessageContext.setResponse(true);
            synapseInMessageContext.setTo(
                new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL));
            synapseInMessageContext.setTracingState(synapseOutMsgCtx.getTracingState());

            Iterator iter = synapseOutMsgCtx.getPropertyKeySet().iterator();

            while (iter.hasNext()) {
                Object key = iter.next();
                synapseInMessageContext.setProperty(
                        (String) key, synapseOutMsgCtx.getProperty((String) key));
            }

            try {
                synapseOutMsgCtx.getEnvironment().injectMessage(synapseInMessageContext);
            } catch (SynapseException syne) {
                Stack stack = synapseInMessageContext.getFaultStack();
                if (stack != null &&
                        !stack.isEmpty()) {
                    ((FaultHandler) stack.pop()).handleFault(synapseInMessageContext, syne);
                } else {
                    log.error("Synapse encountered an exception, " +
                            "No error handlers found - [Message Dropped]\n" + syne.getMessage());
                }
            }
        }
    }

    /**
     * It is possible for us (Synapse) to cause the creation of a duplicate relatesTo as we
     * try to hold onto the outgoing message ID even for POX messages using the relates to
     * Now once we get a response, make sure we remove any trace of this before we proceed any
     * further
     * @param mc the message context from which a possibly duplicated relatesTo should be removed
     * @param relates the existing relatedTo array of the message
     */
    private void removeDuplicateRelatesTo(MessageContext mc, RelatesTo[] relates) {

        int insertPos = 0;
        RelatesTo[] newRelates = new RelatesTo[relates.length];

        for (int i = 0; i < relates.length; i++) {
            RelatesTo current = relates[i];
            boolean found = false;
            for (int j = 0; j < newRelates.length && j < insertPos; j++) {
                if (newRelates[j].equals(current) ||
                        newRelates[j].getValue().equals(current.getValue())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                newRelates[insertPos++] = current;
            }
        }

        RelatesTo[] trimmedRelates = new RelatesTo[insertPos];
        System.arraycopy(newRelates, 0, trimmedRelates, 0, insertPos);
        mc.setRelationships(trimmedRelates);
    }
}
