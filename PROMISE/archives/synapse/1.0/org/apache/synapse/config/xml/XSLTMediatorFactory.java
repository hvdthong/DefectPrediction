package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.OMElementUtils;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.transform.XSLTMediator;
import org.apache.synapse.config.xml.Constants;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.MediatorPropertyFactory;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;

/**
 * Creates a XSLT mediator from the given XML
 *
 * <pre>
 * &lt;xslt key="property-key" [source="xpath"]&gt;
 *   &lt;property name="string" (value="literal" | expression="xpath")/&gt;*
 * &lt;/transform&gt;
 * </pre>
 */
public class XSLTMediatorFactory extends AbstractMediatorFactory {

    private static final Log log = LogFactory.getLog(XSLTMediatorFactory.class);
    private static final QName TAG_NAME    = new QName(Constants.SYNAPSE_NAMESPACE, "xslt");

    public QName getTagQName() {
        return TAG_NAME;
    }

    public Mediator createMediator(OMElement elem) {

        XSLTMediator transformMediator = new XSLTMediator();

        OMAttribute attXslt   = elem.getAttribute(new QName(Constants.NULL_NAMESPACE, "key"));
        OMAttribute attSource = elem.getAttribute(new QName(Constants.NULL_NAMESPACE, "source"));

        if (attXslt != null) {
            transformMediator.setXsltKey(attXslt.getAttributeValue());
        } else {
            handleException("The 'key' attribute is required for the XSLT mediator");
        }

        if (attSource != null) {
            try {
                AXIOMXPath xp = new AXIOMXPath(attSource.getAttributeValue());
                OMElementUtils.addNameSpaces(xp, elem, log);
                transformMediator.setSource(xp);

            } catch (JaxenException e) {
                handleException("Invalid XPath specified for the source attribute : " +
                    attSource.getAttributeValue());
            }
        }
        initMediator(transformMediator,elem);

        transformMediator.addAllProperties(
            MediatorPropertyFactory.getMediatorProperties(elem));

        return transformMediator;
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

}