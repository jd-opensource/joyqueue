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
package io.openmessaging.spring.boot.registry;

import io.openmessaging.producer.TransactionStateCheckListener;
import io.openmessaging.spring.boot.annotation.OMSTransactionStateCheckListener;
import io.openmessaging.spring.support.AccessPointContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Map;

/**
 * Register {@link OMSTransactionStateCheckListener} to BeanDefinitionRegistry
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class TransactionStateCheckListenerRegistrar implements ApplicationContextAware, InitializingBean, BeanPostProcessor {

    private AccessPointContainer accessPointContainer;
    private GenericApplicationContext applicationContext;

    public TransactionStateCheckListenerRegistrar(AccessPointContainer accessPointContainer) {
        this.accessPointContainer = accessPointContainer;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (GenericApplicationContext) applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, Object> listenerMap = applicationContext.getBeansWithAnnotation(OMSTransactionStateCheckListener.class);
        if (listenerMap == null || listenerMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : listenerMap.entrySet()) {
            registerListener(entry.getKey(), entry.getValue());
        }
    }

    protected void registerListener(String beanName, Object bean) {
        if (!(bean instanceof TransactionStateCheckListener)) {
            throw new IllegalArgumentException(String.format("%s (%s) is not TransactionStateCheckListener", beanName, bean.getClass()));
        }
        if (accessPointContainer.getTransactionStateCheckListener() != null) {
            throw new IllegalArgumentException(String.format("transactionStateCheckListener already exists, instance: %s", accessPointContainer.getTransactionStateCheckListener()));
        }

        OMSTransactionStateCheckListener listenerAnnotation = bean.getClass().getAnnotation(OMSTransactionStateCheckListener.class);
        accessPointContainer.setTransactionStateCheckListener((TransactionStateCheckListener) bean);
    }
}