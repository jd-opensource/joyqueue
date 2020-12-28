/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openmessaging.spring.cloud.stream.binder.provisioning;

import io.openmessaging.spring.cloud.stream.binder.properties.OMSConsumerProperties;
import io.openmessaging.spring.cloud.stream.binder.properties.OMSProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;

/**
 * OMS Topic Provisioner
 */
public class OMSTopicProvisioner implements ProvisioningProvider<ExtendedConsumerProperties<OMSConsumerProperties>,
        ExtendedProducerProperties<OMSProducerProperties>> {

    @Override
    public ProducerDestination provisionProducerDestination(String name,
                                                            ExtendedProducerProperties<OMSProducerProperties> properties) throws ProvisioningException {
        checkTopic(name);
        return new OMSProducerDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
                                                            ExtendedConsumerProperties<OMSConsumerProperties> properties) throws ProvisioningException {
        checkTopic(name);
        return new OMSConsumerDestination(name);
    }

    private void checkTopic(String topic) {
        try {
            //Validators.checkTopic(topic);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * OMS Producer Destination
     */
    private static final class OMSProducerDestination implements ProducerDestination {

        private final String producerDestinationName;

        OMSProducerDestination(String destinationName) {
            this.producerDestinationName = destinationName;
        }

        @Override
        public String getName() {
            return producerDestinationName;
        }

        @Override
        public String getNameForPartition(int partition) {
            return producerDestinationName;
        }

    }

    /**
     * OMS Consumer Destination
     */
    private static final class OMSConsumerDestination implements ConsumerDestination {

        private final String consumerDestinationName;

        OMSConsumerDestination(String consumerDestinationName) {
            this.consumerDestinationName = consumerDestinationName;
        }

        @Override
        public String getName() {
            return this.consumerDestinationName;
        }

    }
}
