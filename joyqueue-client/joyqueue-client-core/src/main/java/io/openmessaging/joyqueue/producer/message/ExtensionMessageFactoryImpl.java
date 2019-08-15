package io.openmessaging.joyqueue.producer.message;

import com.google.common.base.Preconditions;
import io.openmessaging.joyqueue.config.ExceptionConverter;
import io.openmessaging.joyqueue.producer.extension.ExtensionMessageFactory;
import io.openmessaging.message.Message;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * ExtensionMessageFactoryImpl
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
public class ExtensionMessageFactoryImpl implements ExtensionMessageFactory {

    @Override
    public Message createMessage(String queueName, byte[] body) {
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(queueName), "queueName can not be null");
            Preconditions.checkArgument(ArrayUtils.isNotEmpty(body), "body can not be null");
        } catch (Throwable cause) {
            throw ExceptionConverter.convertProduceException(cause);
        }

        OMSProduceMessage omsProduceMessage = new OMSProduceMessage();
        omsProduceMessage.setTopic(queueName);
        omsProduceMessage.setBodyBytes(body);

        MessageAdapter messageAdapter = new MessageAdapter(omsProduceMessage);
        omsProduceMessage.setOmsMessage(messageAdapter);
        return messageAdapter;
    }

    @Override
    public Message createMessage(String queueName, String body) {
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(queueName), "queueName can not be null");
            Preconditions.checkArgument(StringUtils.isNotBlank(body), "body can not be null");
        } catch (Throwable cause) {
            throw ExceptionConverter.convertProduceException(cause);
        }

        OMSProduceMessage omsProduceMessage = new OMSProduceMessage();
        omsProduceMessage.setTopic(queueName);
        omsProduceMessage.setBody(body);

        MessageAdapter messageAdapter = new MessageAdapter(omsProduceMessage);
        omsProduceMessage.setOmsMessage(messageAdapter);
        return messageAdapter;
    }
}