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
package org.joyqueue.broker.kafka.network.protocol;

import org.joyqueue.broker.kafka.network.KafkaHeader;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.network.transport.codec.Codec;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhuduohui on 2018/9/2.
 */
public class KafkaHeaderCodec implements Codec {
    private static Logger logger = LoggerFactory.getLogger(KafkaHeaderCodec.class);

    @Override
    public KafkaHeader decode(ByteBuf buffer) throws TransportException.CodecException {
        KafkaHeader kafkaHeader = new KafkaHeader();

        kafkaHeader.setApiKey(buffer.readShort());
        kafkaHeader.setApiVersion(buffer.readShort());
        kafkaHeader.setRequestId(buffer.readInt());
        try {
            kafkaHeader.setClientId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        } catch (Exception e) {
            throw new TransportException.CodecException(e);
        }

        if (kafkaHeader.getDirection() == null) kafkaHeader.setDirection(Direction.REQUEST);
        if (kafkaHeader.getQosLevel() == null) kafkaHeader.setQosLevel(QosLevel.RECEIVE);

        return kafkaHeader;
    }

    @Override
    public void encode(Object obj, ByteBuf buffer) throws TransportException.CodecException {
        KafkaHeader kafkaHeader = (KafkaHeader)obj;
        buffer.writeInt(kafkaHeader.getRequestId());
    }
}
