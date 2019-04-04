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

package io.openmessaging.spring.boot.registry;

import io.openmessaging.consumer.BatchMessageListener;
import io.openmessaging.consumer.MessageListener;
import io.openmessaging.message.Message;
import io.openmessaging.spring.OMSSpringConsts;
import io.openmessaging.spring.boot.adapter.BatchMessageListenerReflectAdapter;
import io.openmessaging.spring.boot.adapter.MessageListenerReflectAdapter;
import io.openmessaging.spring.boot.annotation.OMSMessageListener;
import io.openmessaging.spring.support.AccessPointContainer;
import io.openmessaging.spring.support.ConsumerContainer;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Register {@link OMSMessageListener} to BeanDefinitionRegistry
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class ConsumerRegistrar implements BeanPostProcessor, BeanFactoryAware, EmbeddedValueResolverAware, SmartInitializingSingleton {

    private static final String CONSUMER_CONTAINER_ID = "%s.consumer.%s";

    private final AtomicInteger SEQUENCE = new AtomicInteger();

    private AccessPointContainer accessPointContainer;
    private BeanFactory beanFactory;
    private StringValueResolver stringValueResolver;

    private List<String> consumerIds = new LinkedList<>();

    public ConsumerRegistrar(AccessPointContainer accessPointContainer) {
        this.accessPointContainer = accessPointContainer;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.stringValueResolver = resolver;
    }

    @Override
    public void afterSingletonsInstantiated() {
        for (String consumerId : consumerIds) {
            beanFactory.getBean(consumerId);
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = AopUtils.getTargetClass(bean);
        OMSMessageListener classMessageListenerAnnotation = findClassMessageListenerAnnotation(beanClass);

        if (classMessageListenerAnnotation != null) {
            registerListener(classMessageListenerAnnotation, getMessageListener(classMessageListenerAnnotation, bean));
        } else {
            for (Method method : beanClass.getDeclaredMethods()) {
                OMSMessageListener methodMessageListenerAnnotation = findMethodMessageListenerAnnotation(method);
                if (methodMessageListenerAnnotation == null) {
                    continue;
                }
                Object messageListener = getMethodMessageListener(classMessageListenerAnnotation, bean, method);
                registerListener(methodMessageListenerAnnotation, messageListener);
            }
        }

        return bean;
    }

    protected OMSMessageListener findClassMessageListenerAnnotation(Class<?> beanClass) {
        return AnnotationUtils.findAnnotation(beanClass, OMSMessageListener.class);
    }

    protected OMSMessageListener findMethodMessageListenerAnnotation(Method method) {
        return AnnotationUtils.findAnnotation(method, OMSMessageListener.class);
    }

    protected Object getMessageListener(OMSMessageListener messageListenerAnnotation, Object bean) {
        if (!(bean instanceof MessageListener) && !(bean instanceof BatchMessageListener)) {
            throw new IllegalArgumentException("listener type error, need MessageListener or BatchMessageListener");
        }
        return bean;
    }

    protected Object getMethodMessageListener(OMSMessageListener messageListenerAnnotation, Object bean, Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();

        boolean isListener = (parameterTypes.length == 2 && parameterTypes[0].equals(Message.class) && parameterTypes[1].equals(MessageListener.Context.class));
        boolean isBatchListener = (parameterTypes.length == 2 && genericParameterTypes[0].getTypeName().equals(String.format("java.util.List<%s>", Message.class.getName())) && parameterTypes[1].equals(BatchMessageListener.Context.class));

        if (!isListener && !isBatchListener) {
            throw new IllegalArgumentException("listener parameters error, need MessageListener.onReceived or BatchMessageListener.onReceived");
        }

        if (isListener) {
            return new MessageListenerReflectAdapter(bean, method);
        } else {
            return new BatchMessageListenerReflectAdapter(bean, method);
        }
    }

    protected void registerListener(OMSMessageListener omsMessageListener, Object listenerBean) {
        BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) beanFactory;
        String id = String.format(CONSUMER_CONTAINER_ID, OMSSpringConsts.BEAN_ID_PREFIX, SEQUENCE.getAndIncrement());

        BeanDefinition consumerBeanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ConsumerContainer.class)
                .addConstructorArgValue(stringValueResolver.resolveStringValue(omsMessageListener.queueName()))
                .addConstructorArgValue(accessPointContainer)
                .addConstructorArgValue(listenerBean)
                .getBeanDefinition();

        beanDefinitionRegistry.registerBeanDefinition(id, consumerBeanDefinition);
        consumerIds.add(id);
    }
}