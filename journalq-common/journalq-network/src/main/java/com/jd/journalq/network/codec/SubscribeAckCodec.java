package com.jd.journalq.network.codec;

import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.network.command.*;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.command.SubscribeAck;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/10/16
 */
public class SubscribeAckCodec implements PayloadCodec<Header, SubscribeAck>, Type {
    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        short configSize = buffer.readShort();
        List<TopicConfig> configs = new ArrayList();
        if (configSize > 0) {
            for (int i = 0; i < configSize; i++) {
                configs.add(Serializer.readTopicConfig(buffer));
            }
        }
        return new SubscribeAck().topicConfigs(configs);
    }

    @Override
    public int type() {
        return CommandType.SUBSCRIBE_ACK;
    }

    @Override
    public void encode(SubscribeAck payload, ByteBuf buffer) throws Exception {
        List<TopicConfig> topicConfigs = payload.getTopicConfigs();
        int configSize = topicConfigs == null ? 0 : topicConfigs.size();
        buffer.writeShort(configSize);
        if (configSize > 0) {
            for (TopicConfig config : topicConfigs) {
                Serializer.write(config, buffer);
            }
        }
    }
}
