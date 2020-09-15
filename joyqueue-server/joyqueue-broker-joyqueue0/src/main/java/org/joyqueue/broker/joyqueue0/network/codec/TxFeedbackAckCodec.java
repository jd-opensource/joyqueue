package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.TxFeedbackAck;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Header;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;

/**
 * TxFeedbackAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/8
 */
public class TxFeedbackAckCodec implements PayloadCodec<Joyqueue0Header, TxFeedbackAck>, Type {

    @Override
    public TxFeedbackAck decode(Joyqueue0Header header, ByteBuf buffer) throws Exception {
        return null;
    }

    @Override
    public void encode(TxFeedbackAck payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public int type() {
        return Joyqueue0CommandType.TX_FEEDBACK_ACK.getCode();
    }
}