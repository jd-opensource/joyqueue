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

import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.interceptor.ConsumerInterceptor;
import io.openmessaging.interceptor.ProducerInterceptor;
import io.openmessaging.producer.TransactionStateCheckListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Container for the accessPoint.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class AccessPointContainer implements BeanFactoryAware, InitializingBean {

    private String id;
    private String url;
    private KeyValue attributes;

    private MessagingAccessPoint accessPoint;
    private ListableBeanFactory beanFactory;

    private List<InterceptorContainer> interceptorContainers = new LinkedList<>();
    private TransactionStateCheckListener transactionStateCheckListener;

    public AccessPointContainer(String url, KeyValue attributes) {
        this.url = url;
        this.attributes = attributes;
    }

    public AccessPointContainer(String id, String url, KeyValue attributes) {
        this.id = id;
        this.url = url;
        this.attributes = attributes;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ListableBeanFactory) beanFactory;
    }

    @Override
    public void afterPropertiesSet() {
        accessPoint = OMS.getMessagingAccessPoint(url, attributes);
    }

    public List<ProducerInterceptor> getProducerInterceptors() {
        List<ProducerInterceptor> result = new LinkedList<>();
        List<InterceptorContainer> interceptorContainers = new LinkedList<>();
        Map<String, InterceptorContainer> factoryInterceptorContainers = beanFactory.getBeansOfType(InterceptorContainer.class);

        if (!factoryInterceptorContainers.isEmpty()) {
            interceptorContainers.addAll(factoryInterceptorContainers.values());
        }

        if (!this.interceptorContainers.isEmpty()) {
            interceptorContainers.addAll(this.interceptorContainers);
        }

        for (InterceptorContainer interceptorContainer : interceptorContainers) {
            if (((id == null && interceptorContainer.getAccessPoint() == null)
                    || (id != null && id.equals(interceptorContainer.getAccessPoint())))
                    && (interceptorContainer.getInterceptor() instanceof ProducerInterceptor)) {
                result.add((ProducerInterceptor) interceptorContainer.getInterceptor());
            }
        }
        return result;
    }

    public List<ConsumerInterceptor> getConsumerInterceptors() {
        List<ConsumerInterceptor> result = new LinkedList<>();
        List<InterceptorContainer> interceptorContainers = new LinkedList<>();
        Map<String, InterceptorContainer> factoryInterceptorContainers = beanFactory.getBeansOfType(InterceptorContainer.class);

        if (!factoryInterceptorContainers.isEmpty()) {
            interceptorContainers.addAll(factoryInterceptorContainers.values());
        }

        if (!this.interceptorContainers.isEmpty()) {
            interceptorContainers.addAll(this.interceptorContainers);
        }

        for (InterceptorContainer interceptorContainer : interceptorContainers) {
            if (((id == null && interceptorContainer.getAccessPoint() == null)
                    || (id != null && id.equals(interceptorContainer.getAccessPoint())))
                    && (interceptorContainer.getInterceptor() instanceof ConsumerInterceptor)) {
                result.add((ConsumerInterceptor) interceptorContainer.getInterceptor());
            }
        }
        return result;
    }

    public void setTransactionStateCheckListener(TransactionStateCheckListener transactionStateCheckListener) {
        this.transactionStateCheckListener = transactionStateCheckListener;
    }

    public TransactionStateCheckListener getTransactionStateCheckListener() {
        return transactionStateCheckListener;
    }

    public void addInterceptorContainer(InterceptorContainer interceptorContainer) {
        interceptorContainers.add(interceptorContainer);
    }

    public MessagingAccessPoint getAccessPoint() {
        return accessPoint;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setAttributes(KeyValue attributes) {
        this.attributes = attributes;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public KeyValue getAttributes() {
        return attributes;
    }
}