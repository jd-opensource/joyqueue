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
package io.openmessaging.spring.cloud.stream.binder;

import io.openmessaging.producer.Producer;
import io.openmessaging.spring.cloud.stream.binder.consuming.OMSListenerBindingContainer;
import io.openmessaging.spring.cloud.stream.binder.integration.OMSInboundChannelAdapter;
import io.openmessaging.spring.cloud.stream.binder.integration.OMSMessageHandler;
import io.openmessaging.spring.cloud.stream.binder.integration.OMSMessageSource;
import io.openmessaging.spring.cloud.stream.binder.properties.OMSBinderConfigurationProperties;
import io.openmessaging.spring.cloud.stream.binder.properties.OMSConsumerProperties;
import io.openmessaging.spring.cloud.stream.binder.properties.OMSExtendedBindingProperties;
import io.openmessaging.spring.cloud.stream.binder.properties.OMSProducerProperties;
import io.openmessaging.spring.cloud.stream.binder.provisioning.OMSTopicProvisioner;
import io.openmessaging.spring.support.AccessPointContainer;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.binding.MessageConverterConfigurer;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * OMS Message ChannelBinder
 */
public class OMSMessageChannelBinder extends AbstractMessageChannelBinder<ExtendedConsumerProperties<OMSConsumerProperties>,
        ExtendedProducerProperties<OMSProducerProperties>, OMSTopicProvisioner>
        implements ExtendedPropertiesBinder<MessageChannel, OMSConsumerProperties, OMSProducerProperties> {

    private OMSExtendedBindingProperties extendedBindingProperties;

    private final OMSBinderConfigurationProperties omsBinderConfigurationProperties;

    private final AccessPointContainer accessPointContainer;

    private final Map<String, String> topicInUse = new HashMap<>();

    public OMSMessageChannelBinder(OMSTopicProvisioner provisioningProvider,
                                   OMSExtendedBindingProperties extendedBindingProperties,
                                   OMSBinderConfigurationProperties omsBinderConfigurationProperties,
                                   AccessPointContainer accessPointContainer) {
        super(null, provisioningProvider);
        this.extendedBindingProperties = extendedBindingProperties;
        this.omsBinderConfigurationProperties = omsBinderConfigurationProperties;
        this.accessPointContainer = accessPointContainer;
    }

    /**
     * Do Bind Producer
     *
     * @param destination
     * @param producerProperties
     * @param channel
     * @param errorChannel
     * @return
     * @throws Exception
     */
    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
                                                          ExtendedProducerProperties<OMSProducerProperties> producerProperties,
                                                          MessageChannel channel,
                                                          MessageChannel errorChannel) throws Exception {
        Producer producer = accessPointContainer.getAccessPoint().createProducer();
        MessageConverterConfigurer.PartitioningInterceptor partitioningInterceptor = null;
        if (null != channel) {
            partitioningInterceptor = ((AbstractMessageChannel) channel).getChannelInterceptors().stream()
                    .filter(channelInterceptor -> channelInterceptor instanceof MessageConverterConfigurer.PartitioningInterceptor)
                    .map(channelInterceptor -> ((MessageConverterConfigurer.PartitioningInterceptor) channelInterceptor))
                    .findFirst().orElse(null);
        }
        OMSMessageHandler messageHandler = new OMSMessageHandler(producer, destination.getName(),
                StringUtils.isEmpty(producerProperties.getExtension().getGroup()) ? destination.getName() : producerProperties.getExtension().getGroup(),
                producerProperties.getExtension().getTransactional(),
                producerProperties,
                partitioningInterceptor
        );
        messageHandler.setBeanFactory(this.getApplicationContext().getBeanFactory());
        return messageHandler;
    }

    /**
     * Do Bind Producer with error channel
     *
     * @param destination
     * @param producerProperties
     * @param errorChannel
     * @return
     * @throws Exception
     */
    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
                                                          ExtendedProducerProperties<OMSProducerProperties> producerProperties,
                                                          MessageChannel errorChannel) throws Exception {
        return createProducerMessageHandler(destination, producerProperties, null, errorChannel);
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination,
                                                     String group,
                                                     ExtendedConsumerProperties<OMSConsumerProperties> consumerProperties) throws Exception {
        if (group == null || "".equals(group)) {
            throw new RuntimeException("'group' must be configured for channel " + destination.getName());
        }
        OMSListenerBindingContainer listenerContainer = new OMSListenerBindingContainer(
                consumerProperties, omsBinderConfigurationProperties, this, accessPointContainer);
        listenerContainer.setConsumerGroup(group);
        listenerContainer.setTopic(destination.getName());
        listenerContainer.setConsumeThreadMax(consumerProperties.getConcurrency());
        OMSInboundChannelAdapter omsInboundChannelAdapter = new OMSInboundChannelAdapter(listenerContainer, consumerProperties);
        topicInUse.put(destination.getName(), group);
        ErrorInfrastructure errorInfrastructure = registerErrorInfrastructure(destination, group, consumerProperties);
        if (consumerProperties.getMaxAttempts() > 1) {
            omsInboundChannelAdapter.setRetryTemplate(buildRetryTemplate(consumerProperties));
            omsInboundChannelAdapter.setRecoveryCallback(errorInfrastructure.getRecoverer());
        } else {
            omsInboundChannelAdapter.setErrorChannel(errorInfrastructure.getErrorChannel());
        }
        return omsInboundChannelAdapter;
    }

    @Override
    protected PolledConsumerResources createPolledConsumerResources(String name,
                                                                    String group,
                                                                    ConsumerDestination destination,
                                                                    ExtendedConsumerProperties<OMSConsumerProperties> consumerProperties) {
        OMSMessageSource joyQueueMessageSource = new OMSMessageSource(accessPointContainer,
                omsBinderConfigurationProperties, consumerProperties, name, group);
        return new PolledConsumerResources(joyQueueMessageSource, registerErrorInfrastructure(destination, group, consumerProperties, true));
    }

    @Override
    public OMSConsumerProperties getExtendedConsumerProperties(String channelName) {
        return extendedBindingProperties.getExtendedConsumerProperties(channelName);
    }

    @Override
    public OMSProducerProperties getExtendedProducerProperties(String channelName) {
        return extendedBindingProperties.getExtendedProducerProperties(channelName);
    }

    public Map<String, String> getTopicInUse() {
        return topicInUse;
    }

    @Override
    public String getDefaultsPrefix() {
        return extendedBindingProperties.getDefaultsPrefix();
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return extendedBindingProperties.getExtendedPropertiesEntryClass();
    }

    public void setExtendedBindingProperties(OMSExtendedBindingProperties extendedBindingProperties) {
        this.extendedBindingProperties = extendedBindingProperties;
    }

}
