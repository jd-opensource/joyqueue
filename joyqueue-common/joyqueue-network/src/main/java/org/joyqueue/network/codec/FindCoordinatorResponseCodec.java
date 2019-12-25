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
package org.joyqueue.network.codec;

import com.google.common.collect.Maps;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.FindCoordinatorResponse;
import org.joyqueue.network.command.FindCoordinatorAckData;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.domain.BrokerNode;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * FindCoordinatorResponseCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class FindCoordinatorResponseCodec implements PayloadCodec<JoyQueueHeader, FindCoordinatorResponse>, Type {

    private static final int NONE_BROKER_ID = -1;

    @Override
    public FindCoordinatorResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        FindCoordinatorResponse findCoordinatorResponse = new FindCoordinatorResponse();
        Map<String, FindCoordinatorAckData> coordinators = Maps.newHashMap();

        short topicSize = buffer.readShort();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            BrokerNode node = null;
            int brokerId = buffer.readInt();
            if (brokerId != NONE_BROKER_ID) {
                node = new BrokerNode();
                node.setId(brokerId);
                node.setHost(Serializer.readString(buffer, Serializer.SHORT_SIZE));
                node.setPort(buffer.readInt());
                node.setDataCenter(Serializer.readString(buffer, Serializer.SHORT_SIZE));
                node.setNearby(buffer.readBoolean());
                node.setWeight(buffer.readInt());
            }

            JoyQueueCode code = JoyQueueCode.valueOf(buffer.readInt());
            FindCoordinatorAckData findCoordinatorAckData = new FindCoordinatorAckData(node, code);
            coordinators.put(topic, findCoordinatorAckData);
        }

        findCoordinatorResponse.setCoordinators(coordinators);
        return findCoordinatorResponse;
    }

    @Override
    public void encode(FindCoordinatorResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getCoordinators().size());
        for (Map.Entry<String, FindCoordinatorAckData> entry : payload.getCoordinators().entrySet()) {
            String topic = entry.getKey();
            FindCoordinatorAckData findCoordinatorAckData = entry.getValue();
            BrokerNode node = findCoordinatorAckData.getNode();

            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
            if (node == null) {
                buffer.writeInt(NONE_BROKER_ID);
            } else {
                buffer.writeInt(node.getId());
                Serializer.write(node.getHost(), buffer, Serializer.SHORT_SIZE);
                buffer.writeInt(node.getPort());
                Serializer.write(node.getDataCenter(), buffer, Serializer.SHORT_SIZE);
                buffer.writeBoolean(node.isNearby());
                buffer.writeInt(node.getWeight());
            }
            buffer.writeInt(findCoordinatorAckData.getCode().getCode());
        }
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FIND_COORDINATOR_RESPONSE.getCode();
    }
}