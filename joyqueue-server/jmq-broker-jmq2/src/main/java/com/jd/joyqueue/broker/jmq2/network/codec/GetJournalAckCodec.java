package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.GetJournalAck;
import com.jd.joyqueue.broker.jmq2.network.WrappedByteBuf;
import org.joyqueue.network.transport.codec.PayloadEncoder;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.toolkit.buffer.RByteBuffer;
import io.netty.buffer.ByteBuf;

/**
 * 获取日志响应编码器
 */
public class GetJournalAckCodec implements PayloadEncoder<GetJournalAck>, Type {

    @Override
    public void encode(GetJournalAck payload, ByteBuf out) throws Exception {
        out.writeLong(payload.getOffset());
        out.writeLong(payload.getChecksum());
        RByteBuffer buffer = payload.getBuffer();
        if (buffer != null) {
            out.writeInt(0);
        } else {
            out.writeInt(buffer.remaining());
            out.writeBytes(new WrappedByteBuf(buffer));
        }
    }

    @Override
    public int type() {
        return JMQ2CommandType.GET_JOURNAL_ACK.getCode();
    }
}