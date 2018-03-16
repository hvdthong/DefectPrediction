package org.apache.camel.component.cxf.transport;

/**
 * @version $Revision: 640346 $
 */
public final class CamelConstants {

    public static final String TEXT_MESSAGE_TYPE = "text";
    public static final String BINARY_MESSAGE_TYPE = "binary";
    public static final String CAMEL_TARGET_ENDPOINT_URI = "org.apache.cxf.camel.target.endpoint.uri";
    public static final String CAMEL_SERVER_REQUEST_HEADERS = "org.apache.cxf.camel.server.request.headers";
    public static final String CAMEL_SERVER_RESPONSE_HEADERS = "org.apache.cxf.camel.server.response.headers";
    public static final String CAMEL_REQUEST_MESSAGE = "org.apache.cxf.camel.request.message";
    public static final String CAMEL_RESPONSE_MESSAGE = "org.apache.cxf.camel.reponse.message";
    public static final String CAMEL_CLIENT_REQUEST_HEADERS = "org.apache.cxf.camel.template.request.headers";
    public static final String CAMEL_CLIENT_RESPONSE_HEADERS =
            "org.apache.cxf.camel.template.response.headers";
    public static final String CAMEL_CLIENT_RECEIVE_TIMEOUT = "org.apache.cxf.camel.template.timeout";
    public static final String CAMEL_SERVER_CONFIGURATION_URI =
    public static final String CAMEL_CLIENT_CONFIGURATION_URI =
    public static final String ENDPOINT_CONFIGURATION_URI =
    public static final String SERVICE_CONFIGURATION_URI =
    public static final String PORT_CONFIGURATION_URI =
    public static final String CAMEL_CLIENT_CONFIG_ID = "camel-template";
    public static final String CAMEL_SERVER_CONFIG_ID = "camel-server";
    public static final String CAMEL_REBASED_REPLY_TO = "org.apache.cxf.camel.server.replyto";
    public static final String CAMEL_CORRELATION_ID = "org.apache.cxf.camel.correlationId";

    private CamelConstants() {
    }
}