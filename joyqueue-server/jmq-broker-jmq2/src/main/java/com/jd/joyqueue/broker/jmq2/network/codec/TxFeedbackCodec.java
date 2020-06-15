package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.TxFeedback;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Header;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * TxFeedbackCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/8
 */
public class TxFeedbackCodec implements PayloadCodec<JMQ2Header, TxFeedback>, Type {

    @Override
    public TxFeedback decode(JMQ2Header header, ByteBuf buffer) throws Exception {
        return null;
    }

    @Override
    public void encode(TxFeedback payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public int type() {
        return JMQ2CommandType.TX_FEEDBACK.getCode();
    }
}