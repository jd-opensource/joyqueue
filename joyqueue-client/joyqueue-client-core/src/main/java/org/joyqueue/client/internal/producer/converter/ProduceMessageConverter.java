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
package org.joyqueue.client.internal.producer.converter;

import com.google.common.collect.Lists;
import org.joyqueue.client.internal.common.compress.CompressUtils;
import org.joyqueue.client.internal.common.compress.Compressor;
import org.joyqueue.client.internal.common.compress.CompressorManager;
import org.joyqueue.client.internal.exception.ClientException;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.Message;
import org.joyqueue.message.SourceType;
import org.joyqueue.network.serializer.BatchMessageSerializer;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.time.SystemClock;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * ProduceMessageConverter
 *
 * author: gaohaoxiang
 * date: 2018/12/20
 */
public class ProduceMessageConverter {

    private static final byte[] CLIENT_IP = IpUtil.toByte(IpUtil.getLocalIp() + ":0");

    protected static final Logger logger = LoggerFactory.getLogger(ProduceMessageConverter.class);

    public static List<BrokerMessage> convertToBrokerMessages(String topic, String app, List<ProduceMessage> produceMessages,
                                                              boolean compress, int compressThreshold, String compressType, boolean batch) {
        if (produceMessages.size() == 1) {
            batch = false;
        }
        if (batch) {
            return Lists.newArrayList(convertToBatchBrokerMessage(topic, app, produceMessages, compress, compressThreshold, compressType));
        } else {
            return convertToBrokerMessages(topic, app, produceMessages, compress, compressThreshold, compressType);
        }
    }

    public static List<BrokerMessage> convertToBrokerMessages(String topic, String app, List<ProduceMessage> produceMessages, boolean compress, int compressThreshold, String compressType) {
        List<BrokerMessage> result = Lists.newArrayListWithCapacity(produceMessages.size());
        for (ProduceMessage produceMessage : produceMessages) {
            BrokerMessage brokerMessage = convertToBrokerMessage(topic, app, produceMessage, compress, compressThreshold, compressType);
            result.add(brokerMessage);
        }
        return result;
    }

    public static BrokerMessage convertToBrokerMessage(String topic, String app, ProduceMessage produceMessage, boolean compress, int compressThreshold, String compressType) {
        BrokerMessage brokerMessage = convertToBrokerMessage(topic, app, produceMessage);
        compress(brokerMessage, compress, compressThreshold, compressType);
        return brokerMessage;
    }

    public static BrokerMessage convertToBatchBrokerMessage(String topic, String app, List<ProduceMessage> produceMessages, boolean compress, int compressThreshold, String compressType) {
        List<BrokerMessage> brokerMessages = Lists.newArrayListWithCapacity(produceMessages.size());
        for (ProduceMessage produceMessage : produceMessages) {
            BrokerMessage brokerMessage = convertToBrokerMessage(topic, app, produceMessage);
            brokerMessages.add(brokerMessage);
        }

        ProduceMessage firstProduceMessage = produceMessages.get(0);
        BrokerMessage brokerMessage = new BrokerMessage();
        brokerMessage.setTopic(topic);
        brokerMessage.setApp(app);
        brokerMessage.setPartition(firstProduceMessage.getPartition());
        brokerMessage.setBody(BatchMessageSerializer.serialize(brokerMessages));
        brokerMessage.setPriority(firstProduceMessage.getPriority());
        brokerMessage.setStartTime(SystemClock.now());
        brokerMessage.setFlag((short) produceMessages.size());
        brokerMessage.setSource(SourceType.JOYQUEUE.getValue());
        brokerMessage.setClientIp(CLIENT_IP);
        brokerMessage.setCompressed(false);
        brokerMessage.setBatch(true);

        compress(brokerMessage, compress, compressThreshold, compressType);
        return brokerMessage;
    }

    public static BrokerMessage convertToBrokerMessage(String topic, String app, ProduceMessage produceMessage) {
        BrokerMessage brokerMessage = new BrokerMessage();
        brokerMessage.setTopic(topic);
        brokerMessage.setApp(app);
        brokerMessage.setPartition(produceMessage.getPartition());
        brokerMessage.setBody(serializeBody(produceMessage));
        brokerMessage.setBusinessId(produceMessage.getBusinessId());
        brokerMessage.setPriority(produceMessage.getPriority());
        brokerMessage.setAttributes(produceMessage.getAttributes());
        brokerMessage.setStartTime(SystemClock.now());
        brokerMessage.setFlag(produceMessage.getFlag());
        brokerMessage.setSource(SourceType.JOYQUEUE.getValue());
        brokerMessage.setClientIp(CLIENT_IP);
        brokerMessage.setCompressed(false);
        brokerMessage.setBatch(false);
        return brokerMessage;
    }

    protected static void compress(BrokerMessage brokerMessage, boolean compress, int compressThreshold, String compressType) {
        if (!compress) {
            return;
        }
        byte[] byteBody = brokerMessage.getByteBody();
        if (compressThreshold > byteBody.length) {
            return;
        }
        Compressor compressor = CompressorManager.getCompressor(compressType);
        try {
            brokerMessage.setBody(CompressUtils.compress(brokerMessage.getByteBody(), compressor));
        } catch (IOException e) {
            throw new ClientException(e);
        }
        brokerMessage.setCompressionType(Message.CompressionType.convert(compressor.type()));
        brokerMessage.setCompressed(true);
    }

    protected static byte[] serializeBody(ProduceMessage produceMessage) {
        if (ArrayUtils.isNotEmpty(produceMessage.getBodyBytes())) {
            return produceMessage.getBodyBytes();
        } else {
            try {
                return produceMessage.getBody().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.debug("serializeBody exception, body: {}", produceMessage.getBody(), e);
                return produceMessage.getBody().getBytes();
            }
        }
    }
}