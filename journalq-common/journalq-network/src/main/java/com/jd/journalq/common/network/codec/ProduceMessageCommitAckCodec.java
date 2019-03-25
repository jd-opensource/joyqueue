package com.jd.journalq.common.network.codec;

import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.command.ProduceMessageCommitAck;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessageCommitAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessageCommitAckCodec implements PayloadCodec<JMQHeader, ProduceMessageCommitAck>, Type {

    @Override
    public ProduceMessageCommitAck decode(JMQHeader header, ByteBuf buffer) throws Exception {
        ProduceMessageCommitAck produceMessageCommitAck = new ProduceMessageCommitAck();
        produceMessageCommitAck.setCode(JMQCode.valueOf(buffer.readInt()));
        return produceMessageCommitAck;
    }

    @Override
    public void encode(ProduceMessageCommitAck payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getCode().getCode());
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_COMMIT_ACK.getCode();
    }
}