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
package com.jd.journalq.broker.index.network.codec;

import com.jd.journalq.broker.index.command.ConsumeIndexStoreRequest;
import com.jd.journalq.broker.index.model.IndexAndMetadata;
import com.jd.journalq.network.transport.codec.JournalqHeader;
import com.jd.journalq.network.transport.codec.PayloadDecoder;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Type;

import io.netty.buffer.ByteBuf;
import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class IndexStoreRequestDecoder implements PayloadDecoder<JournalqHeader>, Type {

    @Override
    public Object decode(final JournalqHeader header, final ByteBuf buffer) throws Exception {
        Map<String, Map<Integer, IndexAndMetadata>> indexMetadata = new HashedMap();
        String app = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        int topics = buffer.readInt();
        for (int i = 0; i < topics; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            Map<Integer, IndexAndMetadata> partitionMetadata = new HashedMap();
            int partitions = buffer.readInt();
            for (int j = 0; j < partitions; j++) {
                int partition = buffer.readInt();
                long index = buffer.readLong();
                String metadata = Serializer.readString(buffer, Serializer.SHORT_SIZE);
                long indexCacheRetainTime = buffer.readLong();
                long indexCommitTime = buffer.readLong();
                IndexAndMetadata indexAndMetadata = new IndexAndMetadata(index, metadata);
                partitionMetadata.put(partition, indexAndMetadata);
            }
            indexMetadata.put(topic, partitionMetadata);
        }
        return new ConsumeIndexStoreRequest(app, indexMetadata);
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_STORE_REQUEST;
    }
}
