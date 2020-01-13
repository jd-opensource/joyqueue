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
package io.openmessaging.spring.config;

import io.openmessaging.consumer.BatchMessageListener;
import io.openmessaging.consumer.MessageListener;
import io.openmessaging.spring.OMSSpringConsts;
import io.openmessaging.spring.support.ConsumerContainer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Parser for the consumer element.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class ConsumerBeanDefinitionParser implements BeanDefinitionParser {

    private static final String CONSUMER_CONTAINER_ID = "%s.consumer.container.%s";
    private static final String CONSUMER_ID = "%s.consumer.%s";

    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_ACCESS_POINT = "access-point";
    private static final String ATTRIBUTE_QUEUE_NAME = "queue-name";
    private static final String ATTRIBUTE_LISTENER_CLASS_NAME = "listener";
    private static final String ATTRIBUTE_LISTENER_REF = "listener-ref";

    private final AtomicInteger SEQUENCE = new AtomicInteger();

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String id = element.getAttribute(ATTRIBUTE_ID);
        String accessPoint = element.getAttribute(ATTRIBUTE_ACCESS_POINT);
        String queueName = element.getAttribute(ATTRIBUTE_QUEUE_NAME);

        Assert.hasText(queueName, String.format("%s can not be blank", ATTRIBUTE_QUEUE_NAME));

        if (!StringUtils.hasText(id)) {
            id = String.format(CONSUMER_CONTAINER_ID, OMSSpringConsts.BEAN_ID_PREFIX, SEQUENCE.getAndIncrement());
        }
        if (!StringUtils.hasText(accessPoint)) {
            accessPoint = OMSSpringConsts.DEFAULT_ACCESS_POINT_ID;
        }

        String listenerBeanId = parseListener(element, parserContext);

        BeanDefinitionBuilder consumerBuilder = BeanDefinitionBuilder.rootBeanDefinition(ConsumerContainer.class)
                .addConstructorArgValue(queueName)
                .addConstructorArgReference(accessPoint)
                .addConstructorArgValue(listenerBeanId);

        parserContext.getRegistry().registerBeanDefinition(id, consumerBuilder.getBeanDefinition());
        return consumerBuilder.getBeanDefinition();
    }

    protected String parseListener(Element element, ParserContext parserContext) {
        String listenerClassName = element.getAttribute(ATTRIBUTE_LISTENER_CLASS_NAME);
        String listenerRef = element.getAttribute(ATTRIBUTE_LISTENER_REF);

        Assert.isTrue(StringUtils.hasText(listenerClassName) || StringUtils.hasText(listenerRef), "listener must exist");

        String listenerBeanId = listenerRef;
        BeanDefinition listenerBeanDefinition = null;
        Class<?> listenerClass = null;

        if (StringUtils.hasText(listenerRef)) {
            listenerBeanDefinition = parserContext.getRegistry().getBeanDefinition(listenerRef);
            Assert.notNull(listenerBeanDefinition, String.format("listener not found, listenerName: %s", listenerRef));

            try {
                listenerClass = Class.forName(listenerBeanDefinition.getBeanClassName());
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(String.format("listener class not found, className: %s",
                        listenerBeanDefinition.getBeanClassName()), e);
            }
        } else {
            try {
                listenerClass = Class.forName(listenerClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(String.format("listener class not found, className %s", listenerClassName), e);
            }
            listenerBeanDefinition = BeanDefinitionBuilder.rootBeanDefinition(listenerClass).getBeanDefinition();
            listenerBeanId = String.format(CONSUMER_ID, OMSSpringConsts.BEAN_ID_PREFIX, SEQUENCE.getAndIncrement());
            parserContext.getRegistry().registerBeanDefinition(listenerBeanId, listenerBeanDefinition);
        }

        if (!MessageListener.class.isAssignableFrom(listenerClass) && !BatchMessageListener.class.isAssignableFrom(listenerClass)) {
            throw new IllegalArgumentException(String.format("%s type error, need MessageListener or BatchMessageListener", listenerClassName));
        }

        return listenerBeanId;
    }
}