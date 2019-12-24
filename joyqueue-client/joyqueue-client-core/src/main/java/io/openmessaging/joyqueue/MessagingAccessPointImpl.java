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
package io.openmessaging.joyqueue;

import org.joyqueue.client.internal.MessageAccessPoint;
import org.joyqueue.client.internal.MessageAccessPointFactory;
import org.joyqueue.client.internal.consumer.MessageConsumer;
import org.joyqueue.client.internal.consumer.config.ConsumerConfig;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.producer.MessageProducer;
import org.joyqueue.client.internal.producer.config.ProducerConfig;
import org.joyqueue.client.internal.producer.feedback.config.TxFeedbackConfig;
import org.joyqueue.client.internal.transport.config.TransportConfig;
import org.joyqueue.exception.JoyQueueCode;
import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.consumer.Consumer;
import io.openmessaging.exception.OMSUnsupportException;
import io.openmessaging.joyqueue.config.ExceptionConverter;
import io.openmessaging.joyqueue.config.KeyValueConverter;
import io.openmessaging.joyqueue.consumer.support.ConsumerImpl;
import io.openmessaging.joyqueue.producer.extension.ExtensionMessageFactory;
import io.openmessaging.joyqueue.producer.message.ExtensionMessageFactoryImpl;
import io.openmessaging.joyqueue.producer.support.ProducerImpl;
import io.openmessaging.joyqueue.producer.support.TransactionProducerImpl;
import io.openmessaging.joyqueue.support.ConsumerWrapper;
import io.openmessaging.joyqueue.support.MessageAccessPointHolder;
import io.openmessaging.joyqueue.support.ProducerWrapper;
import io.openmessaging.manager.ResourceManager;
import io.openmessaging.message.MessageFactory;
import io.openmessaging.producer.Producer;
import io.openmessaging.producer.TransactionStateCheckListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MessagingAccessPointImpl
 *
 * author: gaohaoxiang
 * date: 2019/2/18
 */
public class MessagingAccessPointImpl implements MessagingAccessPoint {

    protected final Logger logger = LoggerFactory.getLogger(MessagingAccessPointImpl.class);

    private KeyValue attributes;

    private NameServerConfig nameServerConfig;
    private TransportConfig transportConfig;
    private ProducerConfig producerConfig;
    private ConsumerConfig consumerConfig;
    private TxFeedbackConfig txFeedbackConfig;
    private ExtensionMessageFactory extensionMessageFactory;
    private MessageAccessPointHolder messageAccessPointHolder;

    public MessagingAccessPointImpl(KeyValue attributes) {
        this.attributes = attributes;
        this.nameServerConfig = KeyValueConverter.convertNameServerConfig(attributes);
        this.transportConfig = KeyValueConverter.convertTransportConfig(attributes);
        this.producerConfig = KeyValueConverter.convertProducerConfig(nameServerConfig, attributes);
        this.consumerConfig = KeyValueConverter.convertConsumerConfig(nameServerConfig, attributes);
        this.txFeedbackConfig = KeyValueConverter.convertFeedbackConfig(nameServerConfig, attributes);
        this.extensionMessageFactory = createMessageFactory();
    }

    protected ExtensionMessageFactory createMessageFactory() {
        return new ExtensionMessageFactoryImpl();
    }

    @Override
    public synchronized Producer createProducer() {
        MessageAccessPointHolder messageAccessPointHolder = getOrCreateMessageAccessPointHolder();
        MessageAccessPoint messageAccessPoint = messageAccessPointHolder.getMessageAccessPoint();
        MessageProducer messageProducer = messageAccessPoint.createProducer(producerConfig);
        ProducerImpl producer = new ProducerImpl(messageProducer, extensionMessageFactory);
        return new ProducerWrapper(producer, messageAccessPointHolder);
    }

    @Override
    public synchronized Producer createProducer(TransactionStateCheckListener transactionStateCheckListener) {
        MessageAccessPointHolder messageAccessPointHolder = getOrCreateMessageAccessPointHolder();
        MessageAccessPoint messageAccessPoint = messageAccessPointHolder.getMessageAccessPoint();
        MessageProducer messageProducer = messageAccessPoint.createProducer(producerConfig);
        ProducerImpl producer = new ProducerImpl(messageProducer, extensionMessageFactory);
        TransactionProducerImpl transactionProducer = new TransactionProducerImpl(producer, transactionStateCheckListener, messageProducer, messageAccessPoint, txFeedbackConfig);
        return new ProducerWrapper(transactionProducer, messageAccessPointHolder);
    }

    @Override
    public synchronized Consumer createConsumer() {
        MessageAccessPointHolder messageAccessPointHolder = getOrCreateMessageAccessPointHolder();
        MessageAccessPoint messageAccessPoint = messageAccessPointHolder.getMessageAccessPoint();
        MessageConsumer messageConsumer = messageAccessPoint.createConsumer(consumerConfig);
        ConsumerImpl consumer = new ConsumerImpl(messageConsumer);
        return new ConsumerWrapper(consumer, messageAccessPointHolder);
    }

    protected MessageAccessPointHolder getOrCreateMessageAccessPointHolder() {
        if (messageAccessPointHolder != null && messageAccessPointHolder.getMessageAccessPoint().isStarted()) {
            return messageAccessPointHolder;
        }

        try {
            MessageAccessPoint messageAccessPoint = MessageAccessPointFactory.create(nameServerConfig, transportConfig);
            messageAccessPoint.start();
            messageAccessPointHolder = new MessageAccessPointHolder(messageAccessPoint);
        } catch (Exception e) {
            logger.error("create messagingAccessPoint exception", e);
            throw ExceptionConverter.convertRuntimeException(e);
        }
        return messageAccessPointHolder;
    }

    @Override
    public ResourceManager resourceManager() {
        throw new OMSUnsupportException(JoyQueueCode.CN_COMMAND_UNSUPPORTED.getCode(), "resourceManager is not supported");
    }

    @Override
    public MessageFactory messageFactory() {
        return extensionMessageFactory;
    }

    @Override
    public KeyValue attributes() {
        return attributes;
    }

    @Override
    public String version() {
        return JoyQueueOMSConsts.VERSION;
    }
}