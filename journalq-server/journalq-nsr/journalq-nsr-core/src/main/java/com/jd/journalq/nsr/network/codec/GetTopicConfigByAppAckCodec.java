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
package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetTopicConfigByAppAck;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetTopicConfigByAppAckCodec implements NsrPayloadCodec<GetTopicConfigByAppAck>, Type {
    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        GetTopicConfigByAppAck getTopicConfigByBrokerAck = new GetTopicConfigByAppAck();
            int topicSize = buffer.readInt();
            Map<TopicName,TopicConfig> topicConfigs = new HashMap<>(topicSize);
            for(int i  = 0;i<topicSize;i++){
                TopicConfig topicConfig = Serializer.readTopicConfig(buffer, header.getVersion());
                topicConfigs.put(topicConfig.getName(),topicConfig);
            }
            getTopicConfigByBrokerAck.topicConfigs(topicConfigs);
            return getTopicConfigByBrokerAck;
    }

    @Override
    public void encode(GetTopicConfigByAppAck payload, ByteBuf buffer) throws Exception {
        Map<TopicName, TopicConfig> topicConfigs =  payload.getTopicConfigs();
        if(null==topicConfigs){
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(topicConfigs.size());
        for(TopicConfig topicConfig : topicConfigs.values()){
            Serializer.write(topicConfig,buffer);
        }
    }

    @Override
    public int type() {
        return NsrCommandType.GET_TOPICCONFIGS_BY_APP_ACK;
    }
}
