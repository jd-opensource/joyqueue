package io.openmessaging.journalq.producer.support;

import com.google.common.collect.Lists;
import com.jd.journalq.client.internal.producer.MessageProducer;
import com.jd.journalq.client.internal.producer.callback.AsyncBatchProduceCallback;
import com.jd.journalq.client.internal.producer.callback.AsyncProduceCallback;
import com.jd.journalq.client.internal.producer.domain.ProduceMessage;
import com.jd.journalq.toolkit.lang.Preconditions;
import io.openmessaging.Future;
import io.openmessaging.exception.OMSRuntimeException;
import io.openmessaging.extension.Extension;
import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.interceptor.ProducerInterceptor;
import io.openmessaging.journalq.config.ExceptionConverter;
import io.openmessaging.journalq.producer.ExtensionProducer;
import io.openmessaging.journalq.producer.ExtensionTransactionalResult;
import io.openmessaging.journalq.producer.extension.ExtensionAdapter;
import io.openmessaging.journalq.producer.message.MessageAdapter;
import io.openmessaging.journalq.support.AbstractServiceLifecycle;
import io.openmessaging.message.Message;
import io.openmessaging.message.MessageFactory;
import io.openmessaging.producer.SendResult;
import io.openmessaging.producer.TransactionalResult;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * ProducerImpl
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/19
 */
public class ProducerImpl extends AbstractServiceLifecycle implements ExtensionProducer {

    private MessageProducer messageProducer;
    private MessageFactory messageFactory;
    private Optional<Extension> extension;

    public ProducerImpl(MessageProducer messageProducer, MessageFactory messageFactory) {
        this.messageProducer = messageProducer;
        this.messageFactory = messageFactory;
    }

    @Override
    protected void doStart() throws Exception {
        try {
            messageProducer.start();
        } catch (Throwable cause) {
            throw handleProduceException(cause);
        }
    }

    @Override
    protected void doStop() {
        try {
            messageProducer.stop();
        } catch (Throwable cause) {
            throw handleProduceException(cause);
        }
    }

    @Override
    public SendResult send(Message message) {
        try {
            Preconditions.checkArgument(message instanceof MessageAdapter, "message is not supported");

            MessageAdapter messageAdapter = (MessageAdapter) message;
            com.jd.journalq.client.internal.producer.domain.SendResult sendResult = messageProducer.send(messageAdapter.getProduceMessage());
            return SendResultConverter.convert(sendResult);
        } catch (Throwable cause) {
            throw handleProduceException(cause);
        }
    }

    @Override
    public Future<SendResult> sendAsync(Message message) {
        try {
            Preconditions.checkArgument(message instanceof MessageAdapter, "message is not supported");

            FutureAdapter<SendResult> future = new FutureAdapter<>();
            MessageAdapter messageAdapter = (MessageAdapter) message;
            messageProducer.sendAsync(messageAdapter.getProduceMessage(), new AsyncProduceCallback() {
                @Override
                public void onSuccess(ProduceMessage message, com.jd.journalq.client.internal.producer.domain.SendResult result) {
                    future.setValue(SendResultConverter.convert(result));
                }

                @Override
                public void onException(ProduceMessage message, Throwable cause) {
                    future.setThrowable(handleProduceException(cause));
                }
            });
            return future;
        } catch (Throwable cause) {
            throw handleProduceException(cause);
        }
    }

    @Override
    public void sendOneway(Message message) {
        try {
            Preconditions.checkArgument(message instanceof MessageAdapter, "message is not supported");

            FutureAdapter<SendResult> future = new FutureAdapter<>();
            MessageAdapter messageAdapter = (MessageAdapter) message;
            messageProducer.sendOneway(messageAdapter.getProduceMessage());
        } catch (Throwable cause) {
            throw handleProduceException(cause);
        }
    }

    @Override
    public void send(List<Message> messages) {
        try {
            Preconditions.checkArgument(CollectionUtils.isNotEmpty(messages), "messages can not be null");

            List<ProduceMessage> produceMessages = checkAndConvertMessage(messages);
            messageProducer.batchSend(produceMessages);
        } catch (Throwable cause) {
            throw handleProduceException(cause);
        }
    }

    @Override
    public Future<SendResult> sendAsync(List<Message> messages) {
        try {
            Preconditions.checkArgument(CollectionUtils.isNotEmpty(messages), "messages can not be null");

            FutureAdapter<SendResult> future = new FutureAdapter<>();
            List<ProduceMessage> produceMessages = checkAndConvertMessage(messages);
            messageProducer.batchSendAsync(produceMessages, new AsyncBatchProduceCallback() {
                @Override
                public void onSuccess(List<ProduceMessage> messages, List<com.jd.journalq.client.internal.producer.domain.SendResult> result) {
                    future.setValue(SendResultConverter.convert(result.get(0)));
                }

                @Override
                public void onException(List<ProduceMessage> messages, Throwable cause) {
                    future.setThrowable(cause);
                }
            });
            return future;
        } catch (Throwable cause) {
            throw handleProduceException(cause);
        }
    }

    @Override
    public void sendOneway(List<Message> messages) {
        try {
            Preconditions.checkArgument(CollectionUtils.isNotEmpty(messages), "messages can not be null");

            List<ProduceMessage> produceMessages = checkAndConvertMessage(messages);
            messageProducer.batchSendOneway(produceMessages);
        } catch (Throwable cause) {
            throw handleProduceException(cause);
        }
    }

    @Override
    public void addInterceptor(ProducerInterceptor interceptor) {
        try {
            Preconditions.checkArgument(interceptor != null, "interceptor can not be null");

            messageProducer.addInterceptor(new ProducerInterceptorAdapter(interceptor));
        } catch (Throwable cause) {
            handleProduceException(cause);
        }
    }

    @Override
    public void removeInterceptor(ProducerInterceptor interceptor) {
        try {
            Preconditions.checkArgument(interceptor != null, "interceptor can not be null");

            messageProducer.removeInterceptor(new ProducerInterceptorAdapter(interceptor));
        } catch (Throwable cause) {
            handleProduceException(cause);
        }
    }

    @Override
    public TransactionalResult prepare(Message message) {
        try {
            Preconditions.checkArgument(message instanceof MessageAdapter, "message is not supported");

            String transactionId = message.extensionHeader().get().getTransactionId();
            return new TransactionalResultAdapter(message, transactionId, messageProducer);
        } catch (Throwable cause) {
            throw ExceptionConverter.convertProduceException(cause);
        }
    }

    @Override
    public ExtensionTransactionalResult prepare() {
        try {
            return new ExtensionTransactionalResultAdapter(messageProducer);
        } catch (Throwable cause) {
            throw ExceptionConverter.convertProduceException(cause);
        }
    }

    @Override
    public ExtensionTransactionalResult prepare(String transactionId) {
        try {
            return new ExtensionTransactionalResultAdapter(transactionId, messageProducer);
        } catch (Throwable cause) {
            throw ExceptionConverter.convertProduceException(cause);
        }
    }

    @Override
    public Message createMessage(String queueName, byte[] body) {
        return messageFactory.createMessage(queueName, body);
    }

    @Override
    public Optional<Extension> getExtension() {
        if (extension == null) {
            extension = Optional.of(new ExtensionAdapter(messageProducer));
        }
        return extension;
    }

    @Override
    public QueueMetaData getQueueMetaData(String queueName) {
        return getExtension().get().getQueueMetaData(queueName);
    }

    protected List<ProduceMessage> checkAndConvertMessage(List<Message> messages) {
        List<ProduceMessage> result = Lists.newArrayListWithCapacity(messages.size());
        for (Message message : messages) {
            Preconditions.checkArgument(message instanceof MessageAdapter, "message is not supported");
            result.add(((MessageAdapter) message).getProduceMessage());
        }
        return result;
    }

    protected OMSRuntimeException handleProduceException(Throwable cause) {
        throw ExceptionConverter.convertProduceException(cause);
    }
}