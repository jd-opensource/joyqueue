package com.jd.journalq.broker.election.network.codec;

import com.jd.journalq.broker.election.TopicPartitionGroup;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadDecoder;
import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.broker.election.command.TimeoutNowRequest;
import io.netty.buffer.ByteBuf;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/10/01
 */
public class TimeoutNowRequestDecoder implements PayloadDecoder<JMQHeader>, Type {
    @Override
    public Object decode(final JMQHeader header, final ByteBuf buffer) throws Exception {
        String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        int partitionGroup = buffer.readInt();
        int term = buffer.readInt();
        return new TimeoutNowRequest(new TopicPartitionGroup(topic, partitionGroup), term);
    }

    @Override
    public int type() {
        return CommandType.RAFT_TIMEOUT_NOW_REQUEST;
    }
}
