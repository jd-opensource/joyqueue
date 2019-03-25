package com.jd.journalq.network.codec;

import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.ProduceMessageRollbackAck;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessageRollbackAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessageRollbackAckCodec implements PayloadCodec<JMQHeader, ProduceMessageRollbackAck>, Type {

    @Override
    public ProduceMessageRollbackAck decode(JMQHeader header, ByteBuf buffer) throws Exception {
        ProduceMessageRollbackAck produceMessageRollbackAck = new ProduceMessageRollbackAck();
        produceMessageRollbackAck.setCode(JMQCode.valueOf(buffer.readInt()));
        return produceMessageRollbackAck;
    }

    @Override
    public void encode(ProduceMessageRollbackAck payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getCode().getCode());
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_ROLLBACK_ACK.getCode();
    }
}