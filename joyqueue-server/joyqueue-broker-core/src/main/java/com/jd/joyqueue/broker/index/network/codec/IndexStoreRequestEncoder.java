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
package com.jd.joyqueue.broker.index.network.codec;

import com.jd.joyqueue.broker.index.command.ConsumeIndexStoreRequest;
import com.jd.joyqueue.broker.index.model.IndexAndMetadata;
import com.jd.joyqueue.network.transport.codec.PayloadEncoder;
import com.jd.joyqueue.network.command.CommandType;
import com.jd.joyqueue.network.serializer.Serializer;
import com.jd.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class IndexStoreRequestEncoder implements PayloadEncoder<ConsumeIndexStoreRequest>, Type {

    @Override
    public void encode(final ConsumeIndexStoreRequest request, ByteBuf buffer) throws Exception {
        Map<String, Map<Integer, IndexAndMetadata>> indexMetadata = request.getIndexMetadata();
        Serializer.write(request.getApp(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(indexMetadata.size());
        for (String topic : indexMetadata.keySet()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
            Map<Integer, IndexAndMetadata> partitionMetadata = indexMetadata.get(topic);
            buffer.writeInt(partitionMetadata.size());
            for (int partition : partitionMetadata.keySet()) {
                buffer.writeInt(partition);
                IndexAndMetadata indexAndMetadata = partitionMetadata.get(partition);
                buffer.writeLong(indexAndMetadata.getIndex());
                Serializer.write(indexAndMetadata.getMetadata(), buffer, Serializer.SHORT_SIZE);
                buffer.writeLong(indexAndMetadata.getIndexCacheRetainTime());
                buffer.writeLong(indexAndMetadata.getIndexCommitTime());
            }
        }
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_STORE_REQUEST;
    }
}
