package io.openmessaging.spring.support;

import io.openmessaging.interceptor.ProducerInterceptor;
import io.openmessaging.producer.Producer;
import io.openmessaging.producer.TransactionStateCheckListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Container for the producer.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class ProducerContainer implements InitializingBean, DisposableBean, FactoryBean {

    private AccessPointContainer accessPointContainer;
    private Producer producer;
    private TransactionStateCheckListener transactionStateCheckListener;

    public ProducerContainer(AccessPointContainer accessPointContainer) {
        this(accessPointContainer, null);
    }

    public ProducerContainer(AccessPointContainer accessPointContainer, TransactionStateCheckListener transactionStateCheckListener) {
        this.accessPointContainer = accessPointContainer;
        this.transactionStateCheckListener = transactionStateCheckListener;
    }

    @Override
    public void afterPropertiesSet() {
        if (transactionStateCheckListener == null) {
            transactionStateCheckListener = accessPointContainer.getTransactionStateCheckListener();
        }
        if (transactionStateCheckListener == null) {
            producer = accessPointContainer.getAccessPoint().createProducer();
        } else {
            producer = accessPointContainer.getAccessPoint().createProducer(transactionStateCheckListener);
        }
        for (ProducerInterceptor interceptor : accessPointContainer.getProducerInterceptors()) {
            producer.addInterceptor(interceptor);
        }
        producer.start();
    }

    @Override
    public void destroy() {
        if (producer != null) {
            producer.stop();
        }
    }

    @Override
    public Class<?> getObjectType() {
        return Producer.class;
    }

    @Override
    public Object getObject() {
        return producer;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}