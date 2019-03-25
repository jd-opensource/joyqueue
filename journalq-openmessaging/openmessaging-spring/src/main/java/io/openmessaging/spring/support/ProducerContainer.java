/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
}