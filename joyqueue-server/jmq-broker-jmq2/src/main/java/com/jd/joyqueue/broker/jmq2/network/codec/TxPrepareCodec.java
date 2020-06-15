package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.TxPrepare;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.message.Message;
import org.joyqueue.network.session.TransactionId;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * 分布式事务准备解码器
 */
public class TxPrepareCodec implements JMQ2PayloadCodec, Type {

    @Override
    public Object decode(Header header, final ByteBuf in) throws Exception {
        TxPrepare payload = new TxPrepare();
        //n 字节事务ID
        payload.setTransactionId(new TransactionId(Serializer.readString(in)));
        //事务超时时间
        payload.setTimeout(in.readInt());
        //消息列表
        List<Message> messages = (List) Serializer.readMessages(in);
        payload.setMessages(messages);
        payload.setQueueId(in.readShort());
        return payload;
    }

    @Override
    public void encode(Payload payload, ByteBuf buffer) {

    }

    @Override
    public int type() {
        return JMQ2CommandType.PREPARE.getCode();
    }
}