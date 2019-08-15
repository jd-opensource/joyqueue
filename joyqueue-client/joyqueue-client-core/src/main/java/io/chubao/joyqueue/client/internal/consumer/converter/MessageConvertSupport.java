package io.chubao.joyqueue.client.internal.consumer.converter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.chubao.joyqueue.client.internal.Plugins;
import io.chubao.joyqueue.message.BrokerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * MessageConvertSupport
 *
 * author: gaohaoxiang
 * date: 2019/4/3
 */
public class MessageConvertSupport {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Byte /** type **/, MessageConverter> converterMap;

    public MessageConvertSupport() {
        this.converterMap = loadConverters();
    }

    protected Map<Byte, MessageConverter> loadConverters() {
        Map<Byte, MessageConverter> result = Maps.newHashMap();
        Iterable<MessageConverter> iterable = Plugins.MESSAGE_CONVERTER.extensions();
        for (MessageConverter messageConverter : iterable) {
            result.put(messageConverter.type(), messageConverter);
        }
        return result;
    }

    public List<BrokerMessage> convert(List<BrokerMessage> messages) {
        List<BrokerMessage> result = Lists.newLinkedList();
        for (BrokerMessage message : messages) {
            if (message.isBatch()) {
                List<BrokerMessage> convertedMessages = convertBatch(message);
                if (convertedMessages != null) {
                    result.addAll(convertedMessages);
                } else {
                    result.add(message);
                }
            } else {
                BrokerMessage convertedMessage = convert(message);
                if (convertedMessage != null) {
                    result.add(convertedMessage);
                } else {
                    result.add(message);
                }
            }
        }
        return result;
    }

    public BrokerMessage convert(BrokerMessage message) {
        MessageConverter messageConverter = converterMap.get(message.getSource());
        if (messageConverter == null) {
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("convert message, converter: {}", message.getSource(), messageConverter);
        }
        return messageConverter.convert(message);
    }

    public List<BrokerMessage> convertBatch(BrokerMessage message) {
        MessageConverter messageConverter = converterMap.get(message.getSource());
        if (messageConverter == null) {
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("convert message, converter: {}", message.getSource(), messageConverter);
        }
        return messageConverter.convertBatch(message);
    }
}