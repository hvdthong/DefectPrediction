package org.apache.camel.spring.remoting;


import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.component.bean.BeanProcessor;
import org.apache.camel.util.CamelContextHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.remoting.support.RemoteExporter;

import static org.apache.camel.util.ObjectHelper.notNull;

/**
 * A {@link FactoryBean} to create a proxy to a service exposing a given {@link #getServiceInterface()}
 *
 * @author chirino
 */
public class CamelServiceExporter extends RemoteExporter implements InitializingBean, DisposableBean, ApplicationContextAware, CamelContextAware {
    private String uri;
    private CamelContext camelContext;
    private Consumer consumer;
    private String serviceRef;
    private ApplicationContext applicationContext;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public String getServiceRef() {
        return serviceRef;
    }

    public void setServiceRef(String serviceRef) {
        this.serviceRef = serviceRef;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void afterPropertiesSet() throws Exception {
        notNull(uri, "uri");
        notNull(camelContext, "camelContext");
        if (serviceRef != null && getService() == null && applicationContext != null) {
            setService(applicationContext.getBean(serviceRef));
        }

        Endpoint endpoint = CamelContextHelper.getMandatoryEndpoint(camelContext, uri);
        Object proxy = getProxyForService();

        consumer = endpoint.createConsumer(new BeanProcessor(proxy, camelContext));
        consumer.start();
    }

    public void destroy() throws Exception {
        if (consumer != null) {
            consumer.stop();
        }
    }

}
