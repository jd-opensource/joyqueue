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
package org.joyqueue.nsr.network.codec;

import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.network.NsrPayloadCodec;
import org.joyqueue.nsr.network.command.GetAllTopicsAck;
import org.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetAllTopicsAckCodec implements NsrPayloadCodec<GetAllTopicsAck>, Type {
    @Override
    public GetAllTopicsAck decode(Header header, ByteBuf buffer) throws Exception {
        GetAllTopicsAck getAllTopicsAck = new GetAllTopicsAck();
            int topicsSize = buffer.readInt();
            Set<String> topicNames = new HashSet<>(topicsSize);
            for(int i = 0;i<topicsSize;i++){
                topicNames.add(Serializer.readString(buffer));
            }
            getAllTopicsAck.topicNames(topicNames);
        return getAllTopicsAck;
    }

    @Override
    public void encode(GetAllTopicsAck payload, ByteBuf buffer) throws Exception {
        Set<String> topicNames = payload.getTopicNames();
        if(null==topicNames||topicNames.size()<1){
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(topicNames.size());
        for(String topicName : topicNames){
            Serializer.write(topicName,buffer);
        }
    }

    @Override
    public int type() {
        return NsrCommandType.GET_ALL_TOPICS_ACK;
    }
}
