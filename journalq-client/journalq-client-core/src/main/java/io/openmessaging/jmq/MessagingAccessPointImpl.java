package io.openmessaging.jmq;

import com.jd.journalq.client.internal.MessageAccessPoint;
import com.jd.journalq.client.internal.MessageAccessPointFactory;
import com.jd.journalq.client.internal.consumer.MessageConsumer;
import com.jd.journalq.client.internal.consumer.config.ConsumerConfig;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.producer.MessageProducer;
import com.jd.journalq.client.internal.producer.config.ProducerConfig;
import com.jd.journalq.client.internal.producer.feedback.config.TxFeedbackConfig;
import com.jd.journalq.client.internal.transport.config.TransportConfig;
import com.jd.journalq.common.exception.JMQCode;
import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.consumer.Consumer;
import io.openmessaging.exception.OMSUnsupportException;
import io.openmessaging.jmq.config.ExceptionConverter;
import io.openmessaging.jmq.config.KeyValueConverter;
import io.openmessaging.jmq.consumer.support.ConsumerImpl;
import io.openmessaging.jmq.producer.message.MessageFactoryAdapter;
import io.openmessaging.jmq.producer.support.ProducerImpl;
import io.openmessaging.jmq.producer.support.TransactionProducerImpl;
import io.openmessaging.manager.ResourceManager;
import io.openmessaging.message.MessageFactory;
import io.openmessaging.producer.Producer;
import io.openmessaging.producer.TransactionStateCheckListener;

/**
 * MessagingAccessPointImpl
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/18
 */
public class MessagingAccessPointImpl implements MessagingAccessPoint {

    private KeyValue attributes;

    private NameServerConfig nameServerConfig;
    private TransportConfig transportConfig;
    private ProducerConfig producerConfig;
    private ConsumerConfig consumerConfig;
    private TxFeedbackConfig txFeedbackConfig;
    private MessageFactory messageFactory;
    private MessageAccessPoint messageAccessPoint;

    public MessagingAccessPointImpl(KeyValue attributes) {
        this.attributes = attributes;
        this.nameServerConfig = KeyValueConverter.convertNameServerConfig(attributes);
        this.transportConfig = KeyValueConverter.convertTransportConfig(attributes);
        this.producerConfig = KeyValueConverter.convertProducerConfig(nameServerConfig, attributes);
        this.consumerConfig = KeyValueConverter.convertConsumerConfig(nameServerConfig, attributes);
        this.txFeedbackConfig = KeyValueConverter.convertFeedbackConfig(nameServerConfig, attributes);
        this.messageFactory = createMessageFactory();
        this.messageAccessPoint = createMessageAccessPoint();
    }

    protected MessageFactory createMessageFactory() {
        return new MessageFactoryAdapter();
    }

    protected MessageAccessPoint createMessageAccessPoint() {
        try {
            messageAccessPoint = MessageAccessPointFactory.create(nameServerConfig, transportConfig);
            messageAccessPoint.start();
        } catch (Exception e) {
            throw ExceptionConverter.convertRuntimeException(e);
        }
        return messageAccessPoint;
    }

    @Override
    public Producer createProducer() {
        MessageProducer messageProducer = messageAccessPoint.createProducer(producerConfig);
        return new ProducerImpl(messageProducer, messageFactory);
    }

    @Override
    public Producer createProducer(TransactionStateCheckListener transactionStateCheckListener) {
        MessageProducer messageProducer = messageAccessPoint.createProducer(producerConfig);
        ProducerImpl producer = new ProducerImpl(messageProducer, messageFactory);
        return new TransactionProducerImpl(producer, transactionStateCheckListener, messageProducer, messageAccessPoint, txFeedbackConfig);
    }

    @Override
    public Consumer createConsumer() {
        MessageConsumer messageConsumer = messageAccessPoint.createConsumer(consumerConfig);
        return new ConsumerImpl(messageConsumer);
    }

    @Override
    public ResourceManager resourceManager() {
        throw new OMSUnsupportException(JMQCode.CN_COMMAND_UNSUPPORTED.getCode(), "resourceManager is not supported");
    }

    @Override
    public MessageFactory messageFactory() {
        return messageFactory;
    }

    @Override
    public KeyValue attributes() {
        return attributes;
    }

    @Override
    public String version() {
        return JMQOMSConsts.VERSION;
    }
}