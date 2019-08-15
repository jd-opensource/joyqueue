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
package io.openmessaging.spring.boot.configuration;

import io.openmessaging.KeyValue;
import io.openmessaging.producer.Producer;
import io.openmessaging.spring.boot.OMSSpringBootConsts;
import io.openmessaging.spring.boot.config.KeyValueConverter;
import io.openmessaging.spring.boot.config.OMSProperties;
import io.openmessaging.spring.boot.registry.ConsumerRegistrar;
import io.openmessaging.spring.boot.registry.InterceptorRegistrar;
import io.openmessaging.spring.boot.registry.TransactionStateCheckListenerRegistrar;
import io.openmessaging.spring.support.AccessPointContainer;
import io.openmessaging.spring.support.ProducerContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * Auto-configuration for OpenMessaging.
 *
 * @version OMS 1.0.0
 * @since OMS 1.0.0
 */
@Configuration
@EnableConfigurationProperties(OMSProperties.class)
@ConditionalOnProperty(prefix = OMSSpringBootConsts.PREFIX, name = "url")
public class OMSAutoConfiguration {

    private OMSProperties properties;

    public OMSAutoConfiguration(OMSProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(AccessPointContainer.class)
    public AccessPointContainer createContainer() {
        String url = properties.getUrl();
        Assert.hasText(url, "url can not be blank");

        KeyValue attributes = KeyValueConverter.convert(properties.getAttributes());
        return new AccessPointContainer(url, attributes);
    }

    @Bean
    @ConditionalOnBean(AccessPointContainer.class)
    @ConditionalOnProperty(prefix = OMSSpringBootConsts.PREFIX + ".producer.transaction.check", name = "enable", matchIfMissing = true, havingValue = "true")
    public TransactionStateCheckListenerRegistrar createTransactionStateCheckListenerRegistrar(AccessPointContainer accessPointContainer) {
        return new TransactionStateCheckListenerRegistrar(accessPointContainer);
    }

    @Bean
    @ConditionalOnBean(AccessPointContainer.class)
    @ConditionalOnProperty(prefix = OMSSpringBootConsts.PREFIX + ".interceptor", name = "enable", matchIfMissing = true, havingValue = "true")
    public InterceptorRegistrar createInterceptorRegistrar(AccessPointContainer accessPointContainer) {
        return new InterceptorRegistrar(accessPointContainer);
    }

    @Bean
    @ConditionalOnBean(AccessPointContainer.class)
    @ConditionalOnMissingBean(Producer.class)
    @ConditionalOnProperty(prefix = OMSSpringBootConsts.PREFIX + ".producer", name = "enable", matchIfMissing = true, havingValue = "true")
    public ProducerContainer createProducer(AccessPointContainer accessPointContainer) {
        return new ProducerContainer(accessPointContainer);
    }

    @Bean
    @ConditionalOnBean(AccessPointContainer.class)
    @ConditionalOnProperty(prefix = OMSSpringBootConsts.PREFIX + ".consumer", name = "enable", matchIfMissing = true, havingValue = "true")
    public ConsumerRegistrar createConsumerRegistrar(AccessPointContainer accessPointContainer) {
        return new ConsumerRegistrar(accessPointContainer);
    }
}