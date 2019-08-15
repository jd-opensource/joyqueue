package io.openmessaging.spring.support;

import io.openmessaging.consumer.BatchMessageListener;
import io.openmessaging.consumer.Consumer;
import io.openmessaging.consumer.MessageListener;
import io.openmessaging.interceptor.ConsumerInterceptor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Container for the consumer.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class ConsumerContainer implements InitializingBean, DisposableBean, FactoryBean {

    private String queueName;
    private AccessPointContainer accessPointContainer;
    private Object messageListener;

    private Consumer consumer;

    public ConsumerContainer(String queueName, AccessPointContainer accessPointContainer, Object messageListener) {
        this.queueName = queueName;
        this.accessPointContainer = accessPointContainer;
        this.messageListener = messageListener;
    }

    @Override
    public void afterPropertiesSet() {
        Consumer consumer = accessPointContainer.getAccessPoint().createConsumer();
        consumer.start();

        for (ConsumerInterceptor interceptor : accessPointContainer.getConsumerInterceptors()) {
            consumer.addInterceptor(interceptor);
        }
        if (messageListener instanceof MessageListener) {
            consumer.bindQueue(queueName, (MessageListener) messageListener);
        } else if (messageListener instanceof BatchMessageListener) {
            consumer.bindQueue(queueName, (BatchMessageListener) messageListener);
        } else {
            throw new IllegalArgumentException("listener type error, need MessageListener or BatchMessageListener");
        }

        this.consumer = consumer;
    }

    @Override
    public void destroy() {
        if (consumer != null) {
            consumer.stop();
        }
    }

    @Override
    public Object getObject() throws Exception {
        return consumer;
    }

    @Override
    public Class<?> getObjectType() {
        return Consumer.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}