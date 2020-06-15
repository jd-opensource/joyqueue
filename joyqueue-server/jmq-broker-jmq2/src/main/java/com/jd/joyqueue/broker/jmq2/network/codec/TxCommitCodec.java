package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.TxCommit;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.network.session.TransactionId;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * 分布式事务提交解码器
 */
public class TxCommitCodec implements JMQ2PayloadCodec, Type {

    @Override
    public Object decode(Header header, ByteBuf in) throws Exception {
        TxCommit payload = new TxCommit();
        payload.setTransactionId(new TransactionId(Serializer.readString(in)));
        return payload;
    }

    @Override
    public void encode(Payload payload, ByteBuf buffer) {

    }

    @Override
    public int type() {
        return JMQ2CommandType.COMMIT.getCode();
    }
}