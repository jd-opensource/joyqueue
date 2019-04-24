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
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.network.command.FindCoordinatorResponse;
import com.jd.journalq.network.command.FindCoordinatorAckData;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.domain.BrokerNode;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JournalqHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * FindCoordinatorResponseCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class FindCoordinatorResponseCodec implements PayloadCodec<JournalqHeader, FindCoordinatorResponse>, Type {

    private static final int NONE_BROKER_ID = -1;

    @Override
    public FindCoordinatorResponse decode(JournalqHeader header, ByteBuf buffer) throws Exception {
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

            JournalqCode code = JournalqCode.valueOf(buffer.readInt());
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
        return JournalqCommandType.FIND_COORDINATOR_RESPONSE.getCode();
    }
}