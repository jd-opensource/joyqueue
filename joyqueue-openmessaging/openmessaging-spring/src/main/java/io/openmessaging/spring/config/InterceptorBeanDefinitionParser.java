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

import io.openmessaging.interceptor.ConsumerInterceptor;
import io.openmessaging.interceptor.ProducerInterceptor;
import io.openmessaging.spring.OMSSpringConsts;
import io.openmessaging.spring.support.InterceptorContainer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Parser for the interceptor element.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class InterceptorBeanDefinitionParser implements BeanDefinitionParser {

    private static final String INTERCEPTOR_CONTAINER_ID = "%s.interceptor.container.%s";
    private static final String INTERCEPTOR_ID = "%s.interceptor.%s";

    private static final String ATTRIBUTE_ACCESS_POINT = "access-point";
    private static final String ATTRIBUTE_INTERCEPTOR_CLASS_NAME = "class";
    private static final String ATTRIBUTE_INTERCEPTOR_REF = "ref";

    private final AtomicInteger SEQUENCE = new AtomicInteger();

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String id = String.format(INTERCEPTOR_CONTAINER_ID, OMSSpringConsts.BEAN_ID_PREFIX, SEQUENCE.getAndIncrement());
        String accessPoint = element.getAttribute(ATTRIBUTE_ACCESS_POINT);

        if (!StringUtils.hasText(accessPoint)) {
            accessPoint = OMSSpringConsts.DEFAULT_ACCESS_POINT_ID;
        }

        String interceptorBeanId = parseInterceptor(element, parserContext);

        BeanDefinitionBuilder consumerBuilder = BeanDefinitionBuilder.rootBeanDefinition(InterceptorContainer.class)
                .addConstructorArgValue(accessPoint)
                .addConstructorArgReference(interceptorBeanId);

        parserContext.getRegistry().registerBeanDefinition(id, consumerBuilder.getBeanDefinition());
        return consumerBuilder.getBeanDefinition();
    }

    protected String parseInterceptor(Element element, ParserContext parserContext) {
        String interceptorClassName = element.getAttribute(ATTRIBUTE_INTERCEPTOR_CLASS_NAME);
        String interceptorRef = element.getAttribute(ATTRIBUTE_INTERCEPTOR_REF);

        Assert.isTrue(StringUtils.hasText(interceptorClassName) || StringUtils.hasText(interceptorRef), "interceptor must exist");

        String interceptorBeanId = interceptorRef;
        BeanDefinition interceptorBeanDefinition = null;
        Class<?> interceptorClass = null;

        if (StringUtils.hasText(interceptorRef)) {
            interceptorBeanDefinition = parserContext.getRegistry().getBeanDefinition(interceptorRef);
            Assert.notNull(interceptorBeanDefinition, String.format("interceptor not found, interceptorName: %s", interceptorRef));

            try {
                interceptorClass = Class.forName(interceptorBeanDefinition.getBeanClassName());
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(String.format("interceptor class not found, className: %s",
                        interceptorBeanDefinition.getBeanClassName()), e);
            }
        } else {
            try {
                interceptorClass = Class.forName(interceptorClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(String.format("interceptor class not found, className: %s", interceptorClassName), e);
            }
            interceptorBeanId = String.format(INTERCEPTOR_ID, OMSSpringConsts.BEAN_ID_PREFIX, SEQUENCE.getAndIncrement());
            interceptorBeanDefinition = BeanDefinitionBuilder.rootBeanDefinition(interceptorClass).getBeanDefinition();
            parserContext.getRegistry().registerBeanDefinition(interceptorBeanId, interceptorBeanDefinition);
        }

        if (!ProducerInterceptor.class.isAssignableFrom(interceptorClass) && !ConsumerInterceptor.class.isAssignableFrom(interceptorClass)) {
            throw new IllegalArgumentException(String.format("%s type error, need ProducerInterceptor or ConsumerInterceptor", interceptorClassName));
        }

        return interceptorBeanId;
    }
}