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
package io.openmessaging.spring.cloud.stream.binder.autoconfigure;

import io.openmessaging.KeyValue;
import io.openmessaging.spring.boot.config.KeyValueConverter;
import io.openmessaging.spring.boot.config.OMSProperties;
import io.openmessaging.spring.cloud.stream.binder.properties.OMSBinderConfigurationProperties;
import io.openmessaging.spring.cloud.stream.binder.properties.OMSExtendedBindingProperties;
import io.openmessaging.spring.cloud.stream.binder.utils.BinderUtil;
import io.openmessaging.spring.support.AccessPointContainer;
import io.openmessaging.spring.support.ProducerContainer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * OMS Component for Binder Auto Configuration
 */
@Configuration
@AutoConfigureAfter(OMSBinderAutoConfiguration.class)
@ConditionalOnMissingBean(ProducerContainer.class)
@EnableConfigurationProperties({OMSProperties.class, OMSBinderConfigurationProperties.class, OMSExtendedBindingProperties.class})
public class OMSComponent4BinderAutoConfiguration {

    private final Environment environment;

    private final OMSProperties omsProperties;

    private final OMSExtendedBindingProperties extendedBindingProperties;

    private final OMSBinderConfigurationProperties omsBinderConfigurationProperties;

    public OMSComponent4BinderAutoConfiguration(Environment environment,
                                                OMSProperties omsProperties,
                                                OMSExtendedBindingProperties extendedBindingProperties,
                                                OMSBinderConfigurationProperties omsBinderConfigurationProperties) {
        this.environment = environment;
        this.omsProperties = omsProperties;
        this.extendedBindingProperties = extendedBindingProperties;
        this.omsBinderConfigurationProperties = omsBinderConfigurationProperties;
    }

    @Bean
    @ConditionalOnMissingBean(AccessPointContainer.class)
    public AccessPointContainer createContainer() {
        OMSProperties properties = BinderUtil.mergeProperties(omsBinderConfigurationProperties, omsProperties);
        String omsUrl = properties.getUrl();
        Assert.hasText(omsUrl, "OMS url can not be blank");
        KeyValue attributes = KeyValueConverter.convert(properties.getAttributes());
        return new AccessPointContainer(omsUrl, attributes);
    }

}
