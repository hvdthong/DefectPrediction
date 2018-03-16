package org.apache.synapse.config.xml.endpoints;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.endpoints.DefaultEndpoint;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.utils.EndpointDefinition;

import javax.xml.namespace.QName;

/**
 * Creates {@link DefaultEndpoint} using a XML configuration.
 * <p/>
 * Configuration syntax:
 * <pre>
 * &lt;endpoint [name="<em>name</em>"]&gt;
 *   &lt;default [format="soap11|soap12|pox|get"] [optimize="mtom|swa"]
 *      [encoding="<em>charset encoding</em>"]
 *          [statistics="enable|disable"] [trace="enable|disable"]&gt;
 *     .. extensibility ..
 *
 *     &lt;enableRM [policy="<em>key</em>"]/&gt;?
 *     &lt;enableSec [policy="<em>key</em>"]/&gt;?
 *     &lt;enableAddressing [version="final|submission"] [separateListener="true|false"]/&gt;?
 *
 *     &lt;timeout&gt;
 *       &lt;duration&gt;<em>timeout duration in seconds</em>&lt;/duration&gt;
 *       &lt;action&gt;discard|fault&lt;/action&gt;
 *     &lt;/timeout&gt;?
 *
 *     &lt;suspendDurationOnFailure&gt;
 *       <em>suspend duration in seconds</em>
 *     &lt;/suspendDurationOnFailure&gt;?
 *   &lt;/address&gt;
 * &lt;/endpoint&gt;
 * </pre>
 */
public class DefaultEndpointFactory extends EndpointFactory {

    private static DefaultEndpointFactory instance = new DefaultEndpointFactory();

    protected DefaultEndpointFactory() {
    }

    public static DefaultEndpointFactory getInstance() {
        return instance;
    }

    protected Endpoint createEndpoint(OMElement epConfig, boolean anonymousEndpoint) {

        DefaultEndpoint defaultEndpoint = new DefaultEndpoint();
        OMAttribute name = epConfig.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "name"));

        if (name != null) {
            defaultEndpoint.setName(name.getAttributeValue());
        }

        OMElement defaultElement = epConfig.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "default"));
        if (defaultElement != null) {
            EndpointDefinition endpoint = createEndpointDefinition(defaultElement);
            defaultEndpoint.setEndpoint(endpoint);
        }

        return defaultEndpoint;
    }

    protected void extractSpecificEndpointProperties(EndpointDefinition definition, OMElement elem) {

        OMAttribute format
                = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "format"));
        if (format != null) {
            String forceValue = format.getAttributeValue().trim().toLowerCase();
            if (SynapseConstants.FORMAT_POX.equals(forceValue)) {
                definition.setForcePOX(true);
                definition.setFormat(SynapseConstants.FORMAT_POX);

            } else if (SynapseConstants.FORMAT_GET.equals(forceValue)) {
                definition.setForceGET(true);
                definition.setFormat(SynapseConstants.FORMAT_GET);

            } else if (SynapseConstants.FORMAT_SOAP11.equals(forceValue)) {
                definition.setForceSOAP11(true);
                definition.setFormat(SynapseConstants.FORMAT_SOAP11);

            } else if (SynapseConstants.FORMAT_SOAP12.equals(forceValue)) {
                definition.setForceSOAP12(true);
                definition.setFormat(SynapseConstants.FORMAT_SOAP12);

            } else {
                handleException("force value -\"" + forceValue + "\" not yet implemented");
            }
        }

    }

    /**
     * Creates an EndpointDefinition instance using the XML fragment specification. Configuration
     * for EndpointDefinition always resides inside a configuration of an AddressEndpoint. This
     * factory extracts the details related to the EPR provided for address endpoint.
     *
     * @param elem XML configuration element
     * @return EndpointDefinition object containing the endpoint details.
     */
    public EndpointDefinition createEndpointDefinition(OMElement elem) {
        EndpointDefinition endpointDefinition = new EndpointDefinition();
        extractCommonEndpointProperties(endpointDefinition, elem);
        extractSpecificEndpointProperties(endpointDefinition, elem);
        return endpointDefinition;
    }
}