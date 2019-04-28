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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.command.ProduceMessageAck;
import com.jd.journalq.network.command.ProduceMessageAckData;
import com.jd.journalq.network.command.ProduceMessageAckItemData;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * ProduceMessageAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessageAckCodec implements PayloadCodec<JMQHeader, ProduceMessageAck>, Type {

    @Override
    public ProduceMessageAck decode(JMQHeader header, ByteBuf buffer) throws Exception {
        short dataSize = buffer.readShort();
        Map<String, ProduceMessageAckData> data = Maps.newHashMap();
        for (int i = 0; i < dataSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            JournalqCode code = JournalqCode.valueOf(buffer.readInt());
            short itemSize = buffer.readShort();
            List<ProduceMessageAckItemData> item = Lists.newArrayListWithCapacity(itemSize);

            for (int j = 0; j < itemSize; j++) {
                short partition = buffer.readShort();
                long index = buffer.readLong();
                long startTime = buffer.readLong();
                item.add(new ProduceMessageAckItemData(partition, index, startTime));
            }
            data.put(topic, new ProduceMessageAckData(item, code));
        }

        ProduceMessageAck produceMessageAck = new ProduceMessageAck();
        produceMessageAck.setData(data);
        return produceMessageAck;
    }

    @Override
    public void encode(ProduceMessageAck payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getData().size());
        for (Map.Entry<String, ProduceMessageAckData> entry : payload.getData().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeInt(entry.getValue().getCode().getCode());
            buffer.writeShort(entry.getValue().getItem().size());
            for (ProduceMessageAckItemData data : entry.getValue().getItem()) {
                buffer.writeShort(data.getPartition());
                buffer.writeLong(data.getIndex());
                buffer.writeLong(data.getStartTime());
            }
        }
    }

    @Override
    public int type() {
        return JournalqCommandType.PRODUCE_MESSAGE_ACK.getCode();
    }
}