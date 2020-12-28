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

import io.openmessaging.spring.boot.configuration.OMSAutoConfiguration;
import io.openmessaging.spring.cloud.stream.binder.OMSMessageChannelBinder;
import io.openmessaging.spring.cloud.stream.binder.properties.OMSBinderConfigurationProperties;
import io.openmessaging.spring.cloud.stream.binder.properties.OMSExtendedBindingProperties;
import io.openmessaging.spring.cloud.stream.binder.provisioning.OMSTopicProvisioner;
import io.openmessaging.spring.support.AccessPointContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * OMS Binder Auto Configuration
 */
@Configuration
@Import({OMSAutoConfiguration.class})
@EnableConfigurationProperties({OMSBinderConfigurationProperties.class, OMSExtendedBindingProperties.class})
public class OMSBinderAutoConfiguration {

    private final OMSExtendedBindingProperties extendedBindingProperties;

    private final OMSBinderConfigurationProperties omsBinderConfigurationProperties;

    private final AccessPointContainer accessPointContainer;

    @Autowired
    public OMSBinderAutoConfiguration(
            OMSExtendedBindingProperties extendedBindingProperties,
            OMSBinderConfigurationProperties omsBinderConfigurationProperties,
            AccessPointContainer accessPointContainer) {
        this.extendedBindingProperties = extendedBindingProperties;
        this.omsBinderConfigurationProperties = omsBinderConfigurationProperties;
        this.accessPointContainer = accessPointContainer;
    }

    @Bean
    public OMSTopicProvisioner provisioningProvider() {
        return new OMSTopicProvisioner();
    }

    @Bean
    public OMSMessageChannelBinder omsMessageChannelBinder(OMSTopicProvisioner provisioningProvider) {
        OMSMessageChannelBinder binder = new OMSMessageChannelBinder(
                provisioningProvider, extendedBindingProperties,
                omsBinderConfigurationProperties, accessPointContainer);
        binder.setExtendedBindingProperties(extendedBindingProperties);
        return binder;
    }
}
