package io.openmessaging.spring.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * {@code OMSNamespaceHandler} for the {@code oms} namespace.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class OMSNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("access-point", new AccessPointBeanDefinitionParser());
        registerBeanDefinitionParser("producer", new ProducerBeanDefinitionParser());
        registerBeanDefinitionParser("consumer", new ConsumerBeanDefinitionParser());
        registerBeanDefinitionParser("interceptor", new InterceptorBeanDefinitionParser());
    }
}