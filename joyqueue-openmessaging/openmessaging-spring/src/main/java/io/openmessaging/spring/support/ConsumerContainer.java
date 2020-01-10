/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import io.openmessaging.consumer.BatchMessageListener;
import io.openmessaging.consumer.Consumer;
import io.openmessaging.consumer.MessageListener;
import io.openmessaging.interceptor.ConsumerInterceptor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Container for the consumer.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class ConsumerContainer implements InitializingBean, DisposableBean, FactoryBean, ApplicationContextAware {

    private String queueName;
    private AccessPointContainer accessPointContainer;
    private String messageListenerId;
    private ApplicationContext applicationContext;

    private Consumer consumer;

    public ConsumerContainer(String queueName, AccessPointContainer accessPointContainer, String messageListenerId) {
        this.queueName = queueName;
        this.accessPointContainer = accessPointContainer;
        this.messageListenerId = messageListenerId;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Consumer consumer = accessPointContainer.getAccessPoint().createConsumer();
        consumer.start();

        for (ConsumerInterceptor interceptor : accessPointContainer.getConsumerInterceptors()) {
            consumer.addInterceptor(interceptor);
        }
        Object messageListener = applicationContext.getBean(messageListenerId);
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