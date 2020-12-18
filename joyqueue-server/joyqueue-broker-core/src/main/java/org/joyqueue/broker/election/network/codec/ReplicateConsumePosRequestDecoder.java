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
package org.joyqueue.broker.election.network.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.netty.buffer.ByteBuf;
import org.joyqueue.broker.consumer.model.ConsumePartition;
import org.joyqueue.broker.consumer.position.model.Position;
import org.joyqueue.broker.election.command.ReplicateConsumePosRequest;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadDecoder;
import org.joyqueue.network.transport.command.Type;

import java.util.Map;


/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/29
 */
public class ReplicateConsumePosRequestDecoder implements PayloadDecoder<JoyQueueHeader>, Type {
    @Override
    public Object decode(final JoyQueueHeader header, final ByteBuf buffer) throws Exception {
        String consumePositionString;
        Map<ConsumePartition, Position> consumePositionsMap = null;
        int term = 0;
        int leaderId = 0;
        String topic = null;
        int group = 0;

        if (header.getVersion() == JoyQueueHeader.VERSION_V1) {
            consumePositionString = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        } else {
            consumePositionString = Serializer.readString(buffer, Serializer.INT_SIZE);
        }

        if (consumePositionString != null) {
            consumePositionsMap = JSON.parseObject(consumePositionString, new TypeReference<Map<ConsumePartition, Position>>() {});
        }

        if (header.getVersion() >= JoyQueueHeader.VERSION_V4) {
            term = buffer.readInt();
            leaderId = buffer.readInt();
            topic = Serializer.readString(buffer);
            group = buffer.readInt();
        }

        ReplicateConsumePosRequest replicateConsumePosRequest = new ReplicateConsumePosRequest();
        replicateConsumePosRequest.setConsumePositions(consumePositionsMap);
        replicateConsumePosRequest.setLeaderId(leaderId);
        replicateConsumePosRequest.setTerm(term);
        replicateConsumePosRequest.setTopic(topic);
        replicateConsumePosRequest.setGroup(group);
        return replicateConsumePosRequest;
    }

    @Override
    public int type() {
        return CommandType.REPLICATE_CONSUME_POS_REQUEST;
    }
}
