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

import io.openmessaging.interceptor.ConsumerInterceptor;
import io.openmessaging.interceptor.ProducerInterceptor;
import io.openmessaging.spring.boot.annotation.OMSInterceptor;
import io.openmessaging.spring.support.AccessPointContainer;
import io.openmessaging.spring.support.InterceptorContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Map;

/**
 * Register {@link OMSInterceptor} to BeanDefinitionRegistry
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
public class InterceptorRegistrar implements ApplicationContextAware, InitializingBean, BeanPostProcessor {

    private AccessPointContainer accessPointContainer;
    private GenericApplicationContext applicationContext;

    public InterceptorRegistrar(AccessPointContainer accessPointContainer) {
        this.accessPointContainer = accessPointContainer;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (GenericApplicationContext) applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, Object> interceptorMap = applicationContext.getBeansWithAnnotation(OMSInterceptor.class);
        if (interceptorMap == null || interceptorMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : interceptorMap.entrySet()) {
            registerInterceptor(entry.getKey(), entry.getValue());
        }
    }

    protected void registerInterceptor(String beanName, Object bean) {
        if (!(bean instanceof ProducerInterceptor) && !(bean instanceof ConsumerInterceptor)) {
            throw new IllegalArgumentException(String.format("%s (%s) is not ProducerInterceptor or ConsumerInterceptor", beanName, bean.getClass()));
        }

        OMSInterceptor interceptorAnnotation = bean.getClass().getAnnotation(OMSInterceptor.class);
        InterceptorContainer interceptorContainer = new InterceptorContainer(bean);
        accessPointContainer.addInterceptorContainer(interceptorContainer);
    }

}