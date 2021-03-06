import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.eip.EIPConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.AxisFault;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 *
 */
public class MessageHelper {

    /**
     * This method will simulate cloning the message context and creating an exact copy of the
     * passed message. One should use this method with care; that is because, inside the new MC,
     * most of the attributes of the MC like opCtx and so on are still kept as references inside
     * the axis2 MessageContext for performance improvements. (Note: U dont have to worrie
     * about the SOAPEnvelope, it is a cloned copy and not a reference from any other MC)
     *
     * @param synCtx - this will be cloned 
     * @return cloned Synapse MessageContext
     * @throws AxisFault if there is a failure in creating the new Synapse MC or in a failure in
     *          clonning the underlying axis2 MessageContext
     * 
     * @see MessageHelper#cloneAxis2MessageContext 
     */
    public static MessageContext cloneMessageContext(MessageContext synCtx) throws AxisFault {

        MessageContext newCtx = synCtx.getEnvironment().createMessageContext();
        Axis2MessageContext axis2MC = (Axis2MessageContext) newCtx;
        axis2MC.setAxis2MessageContext(
            cloneAxis2MessageContext(((Axis2MessageContext) synCtx).getAxis2MessageContext()));

        newCtx.setProperty(EIPConstants.AGGREGATE_CORELATION, synCtx.getMessageID());

        newCtx.setTo(synCtx.getTo());
        newCtx.setReplyTo(synCtx.getReplyTo());
        newCtx.setSoapAction(synCtx.getSoapAction());
        newCtx.setWSAAction(synCtx.getWSAAction());

        for (Object o : synCtx.getPropertyKeySet()) {
            if (o instanceof String) {
                newCtx.setProperty((String) o, synCtx.getProperty((String) o));
            }
        }

        return newCtx;
    }

    /**
     * This method will simulate cloning the message context and creating an exact copy of the
     * passed message. One should use this method with care; that is because, inside the new MC,
     * most of the attributes of the MC like opCtx and so on are still kept as references. Otherwise
     * there will be perf issues. But ..... this may reveal in some conflicts in the cloned message
     * if you try to do advanced mediations with the cloned message, in which case you should
     * mannually get a clone of the changing part of the MC and set that cloned part to your MC.
     * Changing the MC after doing that will solve most of the issues. (Note: U dont have to worrie
     * about the SOAPEnvelope, it is a cloned copy and not a reference from any other MC)
     *
     * @param mc - this will be cloned for getting an exact copy
     * @return cloned MessageContext from the given mc
     * @throws AxisFault if there is a failure in copying the certain attributes of the
     *          provided message context
     */
    public static org.apache.axis2.context.MessageContext cloneAxis2MessageContext(
        org.apache.axis2.context.MessageContext mc) throws AxisFault {

        org.apache.axis2.context.MessageContext newMC = clonePartially(mc);
        newMC.setEnvelope(cloneSOAPEnvelope(cloneSOAPEnvelope(mc.getEnvelope())));
        
        newMC.setServiceContext(mc.getServiceContext());
        newMC.setOperationContext(mc.getOperationContext());
        newMC.setAxisMessage(mc.getAxisMessage());
        if (newMC.getAxisMessage() != null) {
            newMC.getAxisMessage().setParent(mc.getAxisOperation());
        }
        newMC.setAxisService(mc.getAxisService());

        newMC.setTransportIn(mc.getTransportIn());
        newMC.setTransportOut(mc.getTransportOut());
        newMC.setProperty(org.apache.axis2.Constants.OUT_TRANSPORT_INFO,
            mc.getProperty(org.apache.axis2.Constants.OUT_TRANSPORT_INFO));

        newMC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS,
            getClonedTransportHeaders(mc));

        return newMC;
    }

    public static Map getClonedTransportHeaders(org.apache.axis2.context.MessageContext msgCtx) {
        
        Map headers = (Map) msgCtx.
            getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Map clonedHeaders = new HashMap();

        if (headers != null && headers.isEmpty()) {
            for (Object o : headers.keySet()) {
                String headerName = (String) o;
                clonedHeaders.put(headerName, headers.get(headerName));
            }
        }

        return clonedHeaders;
    }

    public static org.apache.axis2.context.MessageContext clonePartially(
        org.apache.axis2.context.MessageContext ori) throws AxisFault {

        org.apache.axis2.context.MessageContext newMC
            = new org.apache.axis2.context.MessageContext();
        
        newMC.setConfigurationContext(ori.getConfigurationContext());
        newMC.setMessageID(UUIDGenerator.getUUID());
        newMC.setTo(ori.getTo());
        newMC.setSoapAction(ori.getSoapAction());

        newMC.setProperty(org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING,
                ori.getProperty(org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING));
        newMC.setProperty(org.apache.axis2.Constants.Configuration.ENABLE_MTOM,
                ori.getProperty(org.apache.axis2.Constants.Configuration.ENABLE_MTOM));
        newMC.setProperty(org.apache.axis2.Constants.Configuration.ENABLE_SWA,
                ori.getProperty(org.apache.axis2.Constants.Configuration.ENABLE_SWA));

        newMC.setDoingREST(ori.isDoingREST());
        newMC.setDoingMTOM(ori.isDoingMTOM());
        newMC.setDoingSwA(ori.isDoingSwA());

        Attachments attachments = ori.getAttachmentMap();
        if (attachments != null && attachments.getAllContentIDs().length > 0) {
            String[] cIDs = attachments.getAllContentIDs();
            String soapPart = attachments.getSOAPPartContentID();
            for (String cID : cIDs) {
                if (!cID.equals(soapPart)) {
                    newMC.addAttachment(cID, attachments.getDataHandler(cID));
                }
            }
        }

        for (Object o : ori.getOptions().getProperties().keySet()) {
            String key = (String) o;
            newMC.getOptions().setProperty(key, ori.getOptions().getProperty(key));
        }

        for (Object o1 : ori.getProperties().keySet()) {
            String key = (String) o1;
            newMC.setProperty(key, ori.getProperty(key));
        }

        newMC.setServerSide(false);

        return newMC;
    }

    /**
     * This method will clone the provided SOAPEnvelope and returns the cloned envelope
     * as an exact copy of the provided envelope
     *
     * @param envelope - this will be cloned to get the new envelope
     * @return cloned SOAPEnvelope from the provided one
     */
    public static SOAPEnvelope cloneSOAPEnvelope(SOAPEnvelope envelope) {
        SOAPEnvelope newEnvelope;
        if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI
            .equals(envelope.getBody().getNamespace().getNamespaceURI())) {
            newEnvelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        } else {
            newEnvelope = OMAbstractFactory.getSOAP12Factory().getDefaultEnvelope();
        }

        if (envelope.getHeader() != null) {
            Iterator itr = envelope.getHeader().cloneOMElement().getChildren();
            while (itr.hasNext()) {
                newEnvelope.getHeader().addChild((OMNode) itr.next());
            }
        }

        if (envelope.getBody() != null) {
            Iterator itr = envelope.getBody().cloneOMElement().getChildren();
            while (itr.hasNext()) {
                newEnvelope.getBody().addChild((OMNode) itr.next());
            }
        }

        return newEnvelope;
    }

    /**
     * Removes Submission and Final WS-Addressing headers and return the SOAPEnvelope from the given
     * message context
     *
     * @param axisMsgCtx the Axis2 Message context
     * @return the resulting SOAPEnvelope
     */
    public static SOAPEnvelope removeAddressingHeaders(
        org.apache.axis2.context.MessageContext axisMsgCtx) {

        SOAPEnvelope env = axisMsgCtx.getEnvelope();
        SOAPHeader soapHeader = env.getHeader();
        ArrayList addressingHeaders;

        if (soapHeader != null) {
            addressingHeaders =
                soapHeader.getHeaderBlocksWithNSURI(AddressingConstants.Submission.WSA_NAMESPACE);

            if (addressingHeaders != null && addressingHeaders.size() != 0) {
                detachAddressingInformation(addressingHeaders);

            } else {
                addressingHeaders =
                    soapHeader.getHeaderBlocksWithNSURI(AddressingConstants.Final.WSA_NAMESPACE);
                if (addressingHeaders != null && addressingHeaders.size() != 0) {
                    detachAddressingInformation(addressingHeaders);
                }
            }
        }
        return env;
    }

    /**
     * Remove WS-A headers
     *
     * @param headerInformation headers to be removed
     */
    private static void detachAddressingInformation(ArrayList headerInformation) {
        for (Object o : headerInformation) {
            if (o instanceof SOAPHeaderBlock) {
                SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) o;
                headerBlock.detach();
            } else if (o instanceof OMElement) {
                OMElement om = (OMElement) o;
                OMNamespace ns = om.getNamespace();
                if (ns != null && (
                    AddressingConstants.Submission.WSA_NAMESPACE.equals(ns.getNamespaceURI()) ||
                        AddressingConstants.Final.WSA_NAMESPACE.equals(ns.getNamespaceURI()))) {
                    om.detach();
                }
            }
        }
    }

}
