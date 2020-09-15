package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.TxFeedback;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Header;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;

/**
 * TxFeedbackCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/8
 */
public class TxFeedbackCodec implements PayloadCodec<Joyqueue0Header, TxFeedback>, Type {

    @Override
    public TxFeedback decode(Joyqueue0Header header, ByteBuf buffer) throws Exception {
        return null;
    }

    @Override
    public void encode(TxFeedback payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public int type() {
        return Joyqueue0CommandType.TX_FEEDBACK.getCode();
    }
}