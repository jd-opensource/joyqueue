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
import io.netty.buffer.ByteBuf;
import org.joyqueue.broker.consumer.model.ConsumePartition;
import org.joyqueue.broker.consumer.position.model.Position;
import org.joyqueue.broker.election.command.ReplicateConsumePosRequest;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadEncoder;
import org.joyqueue.network.transport.command.Type;

import java.util.Map;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/29
 */
public class ReplicateConsumePosRequestEncoder implements PayloadEncoder<ReplicateConsumePosRequest>, Type {
    @Override
    public void encode(final ReplicateConsumePosRequest request, ByteBuf buffer) throws Exception {
        Map<ConsumePartition, Position> consumePositions = request.getConsumePositions();
        int bodyLength = Serializer.INT_SIZE;

        if (request.getHeader().getVersion() == JoyQueueHeader.VERSION_V1) {
            bodyLength = Serializer.SHORT_SIZE;
        }

        if (consumePositions == null) {
            Serializer.write((String) null, buffer, bodyLength);
        } else {
            Serializer.write(JSON.toJSONString(request.getConsumePositions()), buffer, bodyLength);
        }

        if (request.getHeader().getVersion() >= JoyQueueHeader.VERSION_V4) {
            buffer.writeInt(request.getTerm());
            buffer.writeInt(request.getLeaderId());
            Serializer.write(request.getTopic(), buffer);
            buffer.writeInt(request.getGroup());
        }
    }

    @Override
    public int type() {
        return CommandType.REPLICATE_CONSUME_POS_REQUEST;
    }
}
