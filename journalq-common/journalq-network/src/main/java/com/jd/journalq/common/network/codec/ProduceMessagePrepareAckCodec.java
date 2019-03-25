package com.jd.journalq.common.network.codec;

import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.command.ProduceMessagePrepareAck;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessagePrepareAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessagePrepareAckCodec implements PayloadCodec<JMQHeader, ProduceMessagePrepareAck>, Type {

    @Override
    public ProduceMessagePrepareAck decode(JMQHeader header, ByteBuf buffer) throws Exception {
        ProduceMessagePrepareAck produceMessagePrepareAck = new ProduceMessagePrepareAck();
        produceMessagePrepareAck.setTxId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepareAck.setCode(JMQCode.valueOf(buffer.readInt()));
        return produceMessagePrepareAck;
    }

    @Override
    public void encode(ProduceMessagePrepareAck payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTxId(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(payload.getCode().getCode());
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_PREPARE_ACK.getCode();
    }
}