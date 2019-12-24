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
package org.joyqueue.broker.index.network.codec;

import org.joyqueue.broker.index.command.ConsumeIndexStoreResponse;
import org.joyqueue.network.transport.codec.PayloadEncoder;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class IndexStoreResponseEncoder implements PayloadEncoder<ConsumeIndexStoreResponse>, Type {

    @Override
    public void encode(final ConsumeIndexStoreResponse response, final ByteBuf buffer) throws Exception {
        Map<String, Map<Integer, Short>> indexStoreStatus = response.getIndexStoreStatus();
        buffer.writeInt(indexStoreStatus.size());
        for (String topic : indexStoreStatus.keySet()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
            Map<Integer, Short> partitionStoreStatus = indexStoreStatus.get(topic);
            buffer.writeInt(partitionStoreStatus.size());
            for (int partition : partitionStoreStatus.keySet()) {
                buffer.writeInt(partition);
                buffer.writeShort(partitionStoreStatus.get(partition));
            }
        }
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_STORE_RESPONSE;
    }
}
