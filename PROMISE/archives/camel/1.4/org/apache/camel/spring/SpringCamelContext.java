package org.apache.camel.spring;

import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.bean.BeanProcessor;
import org.apache.camel.component.event.EventComponent;
import org.apache.camel.component.event.EventEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.ProcessorEndpoint;
import org.apache.camel.spi.Injector;
import org.apache.camel.spi.Registry;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.apache.camel.spring.spi.SpringInjector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A Spring aware implementation of {@link org.apache.camel.CamelContext} which
 * will automatically register itself with Springs lifecycle methods plus allows
 * spring to be used to customize a any <a
 * as well as supporting accessing components and beans via the Spring
 * {@link ApplicationContext}
 *
 * @version $Revision: 655516 $
 */
public class SpringCamelContext extends DefaultCamelContext implements InitializingBean, DisposableBean,
    ApplicationContextAware, ApplicationListener {
    private static final transient Log LOG = LogFactory.getLog(SpringCamelContext.class);
    private ApplicationContext applicationContext;
    private EventEndpoint eventEndpoint;

    public SpringCamelContext() {
    }

    public SpringCamelContext(ApplicationContext applicationContext) {
        setApplicationContext(applicationContext);
    }

    public static SpringCamelContext springCamelContext(ApplicationContext applicationContext)
        throws Exception {
        String[] names = applicationContext.getBeanNamesForType(SpringCamelContext.class);
        if (names.length == 1) {
            return (SpringCamelContext)applicationContext.getBean(names[0], SpringCamelContext.class);
        }
        SpringCamelContext answer = new SpringCamelContext();
        answer.setApplicationContext(applicationContext);
        answer.afterPropertiesSet();
        return answer;
    }
        

    public static SpringCamelContext springCamelContext(String configLocations) throws Exception {
        return springCamelContext(new ClassPathXmlApplicationContext(configLocations));
    }

    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void destroy() throws Exception {
        stop();
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Publishing event: " + event);
        }

        if (event instanceof ContextRefreshedEvent) {
            try {
                LOG.debug("Starting the CamelContext now that the ApplicationContext has started");
                start();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeCamelException(e);
            }
            if (eventEndpoint != null) {
                eventEndpoint.onApplicationEvent(event);
            }
        } else {
            if (eventEndpoint != null) {
                eventEndpoint.onApplicationEvent(event);
            } else {
                LOG.warn("No eventEndpoint enabled for event: " + event);
            }
        }
    }


    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

        if (applicationContext instanceof ConfigurableApplicationContext) {
            addComponent("event", new EventComponent(applicationContext));
        }
    }

    public EventEndpoint getEventEndpoint() {
        return eventEndpoint;
    }

    public void setEventEndpoint(EventEndpoint eventEndpoint) {
        this.eventEndpoint = eventEndpoint;
    }


    @Override
    protected void doStart() throws Exception {
        super.doStart();
        if (eventEndpoint == null) {
            eventEndpoint = createEventEndpoint();
        }
    }

    @Override
    protected Injector createInjector() {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            return new SpringInjector((ConfigurableApplicationContext)applicationContext);
        } else {
            LOG.warn("Cannot use SpringInjector as applicationContext is not a ConfigurableApplicationContext as its: "
                      + applicationContext);
            return super.createInjector();
        }
    }

    protected EventEndpoint createEventEndpoint() {
        EventEndpoint endpoint = getEndpoint("event:default", EventEndpoint.class);
        return endpoint;
    }

    protected Endpoint convertBeanToEndpoint(String uri, Object bean) {
        Endpoint endpoint = getTypeConverter().convertTo(Endpoint.class, bean);
        if (endpoint != null) {
            endpoint.setCamelContext(this);
            return endpoint;
        }
        Processor processor = new BeanProcessor(bean, this);
        return new ProcessorEndpoint(uri, this, processor);
    }

    @Override
    protected Registry createRegistry() {
        return new ApplicationContextRegistry(getApplicationContext());
    }
}
