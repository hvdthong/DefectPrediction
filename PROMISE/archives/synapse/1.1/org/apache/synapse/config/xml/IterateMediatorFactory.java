package org.apache.synapse.config.xml;

import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.eip.splitter.IterateMediator;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;

/**
 * The &lt;iterate&gt; element is used to split messages in Synapse to smaller messages with only
 * one part of the elements described in the XPATH expression.
 * <p/>
 * <pre>
 *  &lt;iterate continueParent=(true | false) preservePayload=(true | false)
 *                          (attachPath="XPATH expression")? expression="XPATH expression"&gt;
 *   &lt;target to="TO address" [soapAction="urn:Action"] sequence="sequence ref"
 *                                                         endpoint="endpoint ref"&gt;
 *    &lt;sequence&gt; (mediator +) &lt;/sequence&gt;
 *    &lt;endpoint&gt; endpoint &lt;/endpoint&gt;
 *   &lt;/target&gt;
 *  &lt;/iterate&gt;
 * </pre>
 */
public class IterateMediatorFactory extends AbstractMediatorFactory {

    private static final Log log = LogFactory.getLog(IterateMediatorFactory.class);

    /**
     * Holds the QName for the IterateMeditor xml configuration
     */
    private static final QName ITERATE_Q = new QName(SynapseConstants.SYNAPSE_NAMESPACE, "iterate");
    private static final QName ATT_CONTPAR = new QName("continueParent");
    private static final QName ATT_PREPLD = new QName("preservePayload");
    private static final QName ATT_ATTACHPATH = new QName("attachPath");

    /**
     * This method will create the IterateMediator by parsing the given xml configuration
     *
     * @param elem OMElement describing the configuration of the IterateMediaotr
     * @return IterateMediator created from the given configuration
     */
    public Mediator createMediator(OMElement elem) {

        IterateMediator mediator = new IterateMediator();
        processTraceState(mediator, elem);

        OMAttribute continueParent = elem.getAttribute(ATT_CONTPAR);
        if (continueParent != null) {
            mediator.setContinueParent(
                Boolean.valueOf(continueParent.getAttributeValue()).booleanValue());
        }

        OMAttribute preservePayload = elem.getAttribute(ATT_PREPLD);
        if (preservePayload != null) {
            mediator.setPreservePayload(
                Boolean.valueOf(preservePayload.getAttributeValue()).booleanValue());
        }

        OMAttribute expression = elem.getAttribute(ATT_EXPRN);
        if (expression != null) {
            try {
                AXIOMXPath xp = new AXIOMXPath(expression.getAttributeValue());
                OMElementUtils.addNameSpaces(xp, elem, log);
                mediator.setExpression(xp);
            } catch (JaxenException e) {
                handleException("Unable to build the IterateMediator. " + "Invalid XPATH " +
                    expression.getAttributeValue(), e);
            }
        } else {
            handleException("XPATH expression is required " +
                "for an IterateMediator under the \"expression\" attribute");
        }

        OMAttribute attachPath = elem.getAttribute(ATT_ATTACHPATH);
        String attachPathValue = ".";
        if (attachPath != null && !mediator.isPreservePayload()) {
            handleException("Wrong configuration for the iterate mediator :: if the iterator " +
                "should not preserve payload, then attachPath can not be present");
        } else if (attachPath != null) {
            attachPathValue = attachPath.getAttributeValue();
        }
        
        try {
            AXIOMXPath xp = new AXIOMXPath(attachPathValue);
            OMElementUtils.addNameSpaces(xp, elem, log);
            mediator.setAttachPath(xp);
        } catch (JaxenException e) {
            handleException("Unable to build the IterateMediator. Invalid XPATH " +
                attachPathValue, e);
        }

        OMElement targetElement = elem.getFirstChildWithName(TARGET_Q);
        if (targetElement != null) {
            mediator.setTarget(TargetFactory.createTarget(targetElement));
        } else {
            handleException("Target for an iterate mediator is required :: missing target");
        }

        return mediator;
    }

    /**
     * Get the IterateMediator configuration tag name
     *
     * @return QName specifying the IterateMediator tag name of the xml configuration
     */
    public QName getTagQName() {
        return ITERATE_Q;
    }
}
