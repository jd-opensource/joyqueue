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
package org.joyqueue.broker.consumer;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.joyqueue.broker.Plugins;
import org.joyqueue.message.BrokerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * MessageConvertSupport
 *
 * author: gaohaoxiang
 * date: 2019/4/3
 */
public class MessageConvertSupport {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Table<Byte /** type **/, Byte /** target **/, MessageConverter> converterTable;

    public MessageConvertSupport() {
        this.converterTable = loadConverters();
    }

    protected Table<Byte, Byte, MessageConverter> loadConverters() {
        Table<Byte, Byte, MessageConverter> result = HashBasedTable.create();
        Iterable<MessageConverter> iterable = Plugins.MESSAGE_CONVERTER.extensions();
        for (MessageConverter messageConverter : iterable) {
            result.put(messageConverter.type(), messageConverter.target(), messageConverter);
        }
        return result;
    }

    public List<BrokerMessage> convert(BrokerMessage message, byte target) {
        List<BrokerMessage> result = Lists.newLinkedList();
        if (message.isBatch()) {
            List<BrokerMessage> convertedMessages = doConvertBatch(message, target);
            if (convertedMessages != null) {
                result.addAll(convertedMessages);
            } else {
                result.add(message);
            }
        } else {
            BrokerMessage convertedMessage = doConvert(message, target);
            if (convertedMessage != null) {
                result.add(convertedMessage);
            } else {
                result.add(message);
            }
        }
        return result;
    }

    public List<BrokerMessage> convert(List<BrokerMessage> messages, byte target) {
        List<BrokerMessage> result = Lists.newLinkedList();
        for (BrokerMessage message : messages) {
            if (message.isBatch()) {
                List<BrokerMessage> convertedMessages = doConvertBatch(message, target);
                if (convertedMessages != null) {
                    result.addAll(convertedMessages);
                } else {
                    result.add(message);
                }
            } else {
                BrokerMessage convertedMessage = doConvert(message, target);
                if (convertedMessage != null) {
                    result.add(convertedMessage);
                } else {
                    result.add(message);
                }
            }
        }
        return result;
    }

    protected BrokerMessage doConvert(BrokerMessage message, byte target) {
        MessageConverter messageConverter = converterTable.get(message.getSource(), target);
        if (messageConverter == null) {
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("convert message {} to {}, converter: {}", message.getSource(), target, messageConverter);
        }
        return messageConverter.convert(message);
    }

    protected List<BrokerMessage> doConvertBatch(BrokerMessage message, byte target) {
        MessageConverter messageConverter = converterTable.get(message.getSource(), target);
        if (messageConverter == null) {
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("convert message {} to {}, converter: {}", message.getSource(), target, messageConverter);
        }
        return messageConverter.convertBatch(message);
    }
}