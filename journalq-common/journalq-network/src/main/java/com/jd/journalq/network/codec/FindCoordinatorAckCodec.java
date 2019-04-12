/**
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
package com.jd.journalq.network.codec;

import com.google.common.collect.Maps;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.command.FindCoordinatorAck;
import com.jd.journalq.network.command.FindCoordinatorAckData;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.domain.BrokerNode;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * FindCoordinatorAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class FindCoordinatorAckCodec implements PayloadCodec<JMQHeader, FindCoordinatorAck>, Type {

    private static final int NONE_BROKER_ID = -1;

    @Override
    public FindCoordinatorAck decode(JMQHeader header, ByteBuf buffer) throws Exception {
        FindCoordinatorAck findCoordinatorAck = new FindCoordinatorAck();
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

            JMQCode code = JMQCode.valueOf(buffer.readInt());
            FindCoordinatorAckData findCoordinatorAckData = new FindCoordinatorAckData(node, code);
            coordinators.put(topic, findCoordinatorAckData);
        }

        findCoordinatorAck.setCoordinators(coordinators);
        return findCoordinatorAck;
    }

    @Override
    public void encode(FindCoordinatorAck payload, ByteBuf buffer) throws Exception {
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
        return JMQCommandType.FIND_COORDINATOR_ACK.getCode();
    }
}