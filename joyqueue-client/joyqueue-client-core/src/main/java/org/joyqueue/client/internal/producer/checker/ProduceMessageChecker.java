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
package org.joyqueue.client.internal.producer.checker;

import org.joyqueue.client.internal.producer.config.ProducerConfig;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.client.internal.producer.exception.ProducerException;
import org.joyqueue.exception.JoyQueueCode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * ProduceMessageChecker
 *
 * author: gaohaoxiang
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
        if (produceMessage.getPartition() != ProduceMessage.NONE_PARTITION && produceMessage.getPartition() < 0) {
            throwCheckException("message body does not exist");
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
        short partition = -1;
        int length = 0;
        for (ProduceMessage produceMessage : produceMessages) {
            checkMessage(produceMessage, config);

            // 确认一批消息是否是同一个topic
            if (topic == null) {
                topic = produceMessage.getTopic();
            } else if (!produceMessage.getTopic().equals(topic)) {
                throwCheckException("batch messages must single topic");
            }

            if (partition == -1) {
                partition = produceMessage.getPartition();
            } else if (produceMessage.getPartition() != partition) {
                throwCheckException("batch messages must single partition");
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
