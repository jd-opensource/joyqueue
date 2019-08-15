package io.chubao.joyqueue.client.internal.producer.checker;

import io.chubao.joyqueue.client.internal.producer.config.ProducerConfig;
import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.chubao.joyqueue.client.internal.producer.exception.ProducerException;
import io.chubao.joyqueue.exception.JoyQueueCode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * ProduceMessageChecker
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/20
 */
public class ProduceMessageChecker {

    public static void checkMessage(ProduceMessage produceMessage, ProducerConfig config) {
        if (produceMessage == null) {
            throwCheckException("message not null");
        }
        if (StringUtils.isBlank(produceMessage.getTopic())) {
            throwCheckException("message topic is not empty");
        }
        if (StringUtils.isBlank(produceMessage.getBody()) && ArrayUtils.isEmpty(produceMessage.getBodyBytes())) {
            throwCheckException("message body is not empty");
        }
        if (StringUtils.isNotBlank(produceMessage.getBody()) && produceMessage.getBody().length() > config.getBodyLengthLimit()) {
            throwCheckException(String.format("body is too long, it must less than %s characters", config.getBodyLengthLimit()));
        }
        if (ArrayUtils.isNotEmpty(produceMessage.getBodyBytes()) && produceMessage.getBodyBytes().length > config.getBodyLengthLimit()) {
            throwCheckException(String.format("bodyBytes is too long, it must less than %s characters", config.getBodyLengthLimit()));
        }
        if (StringUtils.isNotBlank(produceMessage.getBusinessId()) && produceMessage.getBusinessId().length() > config.getBusinessIdLengthLimit()) {
            throwCheckException(String.format("businessId is too long, it must less than %s characters", config.getBusinessIdLengthLimit()));
        }
    }

    public static void checkMessages(List<ProduceMessage> produceMessages, ProducerConfig config) {
        String topic = null;
        int length = 0;
        for (ProduceMessage produceMessage : produceMessages) {
            checkMessage(produceMessage, config);

            // 确认一批消息是否是同一个topic
            if (topic == null) {
                topic = produceMessage.getTopic();
            } else if (!produceMessage.getTopic().equals(topic)) {
                throwCheckException("batch messages must single topic");
            }

            // 计算总长度
            if (StringUtils.isNotBlank(produceMessage.getBody())) {
                length += produceMessage.getBody().length();
            } else {
                length += produceMessage.getBodyBytes().length;
            }
        }

        if (length > config.getBatchBodyLengthLimit()) {
            throwCheckException(String.format("messages body is too long, it must less than %s characters", config.getBatchBodyLengthLimit()));
        }
    }

    protected static void throwCheckException(String message) {
        throw new ProducerException(message, JoyQueueCode.CN_PARAM_ERROR.getCode());
    }
}