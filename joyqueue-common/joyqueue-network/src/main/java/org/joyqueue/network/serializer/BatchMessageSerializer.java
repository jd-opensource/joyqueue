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
package org.joyqueue.network.serializer;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.joyqueue.message.BrokerMessage;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * BatchMessageSerializer
 *
 * author: gaohaoxiang
 * date: 2019/4/2
 */
public class BatchMessageSerializer {

    private static final Logger logger = LoggerFactory.getLogger(BatchMessageSerializer.class);

    private static final int FIX_LENGTH =
            4 // size
                    + 2 // flag
                    + 1 // priority
                    + 4 // body length
                    + 1 // biz id length
                    + 2 // prop length
    ;

    public static int sizeOf(BrokerMessage message) {
        int bodyLength = FIX_LENGTH;

        ByteBuffer buffer = message.getBody();
        int length = buffer == null ? 0 : buffer.remaining();
        bodyLength += length;

        byte[] bytes;

        bytes = Serializer.getBytes(message.getBusinessId(), Charsets.UTF_8);
        bodyLength += bytes == null? 0: bytes.length;

        bytes = Serializer.getBytes(Serializer.toProperties(message.getAttributes()), Charsets.UTF_8);
        bodyLength += bytes == null? 0: bytes.length;

        return bodyLength;
    }

    public static byte[] serialize(List<BrokerMessage> messages) {
        int totalSize = 0;
        for (BrokerMessage message : messages) {
            totalSize += sizeOf(message);
        }

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        for (BrokerMessage message : messages) {
            int startPosition = buffer.position();
            buffer.putInt(0);
            buffer.put(message.getPriority());
            buffer.putShort(message.getFlag());

            // 消息体
            Serializer.write(message.getByteBody(), buffer, Serializer.INT_SIZE);
            // 业务id
            Serializer.write(message.getBusinessId(), buffer, Serializer.BYTE_SIZE);
            // 属性
            Serializer.write(Serializer.toProperties(message.getAttributes()), buffer, Serializer.SHORT_SIZE);
            // 总长度
            buffer.putInt(startPosition, buffer.position() - startPosition);
        }

        return buffer.array();
    }

    public static List<BrokerMessage> deserialize(BrokerMessage brokerMessage) {
        List<BrokerMessage> result = Lists.newArrayListWithCapacity(brokerMessage.getFlag());
        ByteBuffer bodyBuffer = ByteBuffer.wrap(brokerMessage.getByteBody());

        for (int i = 0; i < brokerMessage.getFlag(); i++) {
            int size = bodyBuffer.getInt();
            byte priority = bodyBuffer.get();
            short flag = bodyBuffer.getShort();

            byte[] body = new byte[bodyBuffer.getInt()];
            bodyBuffer.get(body);
            byte[] businessId = new byte[bodyBuffer.get()];
            bodyBuffer.get(businessId);
            byte[] attributes = new byte[bodyBuffer.getShort()];
            bodyBuffer.get(attributes);

            BrokerMessage message = new BrokerMessage();
            message.setTopic(brokerMessage.getTopic());
            message.setApp(brokerMessage.getApp());
            message.setPartition(brokerMessage.getPartition());
            message.setStartTime(brokerMessage.getStartTime());
            message.setSource(brokerMessage.getSource());
            message.setClientIp(brokerMessage.getClientIp());
            message.setCompressed(false);
            message.setBatch(brokerMessage.isBatch());
            message.setMsgIndexNo(brokerMessage.getMsgIndexNo() + i);

            message.setBody(body);
            message.setBusinessId(ArrayUtils.isEmpty(businessId) ? null : new String(businessId, Charsets.UTF_8));
            message.setPriority(priority);
            message.setFlag(flag);
            message.setSize(size);
            try {
                message.setAttributes(ArrayUtils.isEmpty(attributes) ? null : Serializer.toStringMap(new String(attributes, Charsets.UTF_8)));
            } catch (IOException e) {
                logger.debug("", e);
            }

            result.add(message);
        }
        return result;
    }
}