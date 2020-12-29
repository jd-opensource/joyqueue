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
package io.openmessaging.spring.cloud.stream.binder.properties;

import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 * OMS Binding Properties
 */
public class OMSBindingProperties implements BinderSpecificPropertiesProvider {

	@NestedConfigurationProperty
	private OMSConsumerProperties consumer = new OMSConsumerProperties();

	@NestedConfigurationProperty
	private OMSProducerProperties producer = new OMSProducerProperties();

	@Override
	public OMSConsumerProperties getConsumer() {
		return consumer;
	}

	@Override
	public OMSProducerProperties getProducer() {
		return producer;
	}
}
