package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetTopicConfigByBrokerAck;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetTopicConfigByBrokerAckCodec implements NsrPayloadCodec<GetTopicConfigByBrokerAck>, Type {
    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        GetTopicConfigByBrokerAck getTopicConfigByBrokerAck = new GetTopicConfigByBrokerAck();
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
    public void encode(GetTopicConfigByBrokerAck payload, ByteBuf buffer) throws Exception {
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
        return NsrCommandType.GET_TOPICCONFIGS_BY_BROKER_ACK;
    }
}
