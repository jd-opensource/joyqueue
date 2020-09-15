package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.GetJournal;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.transport.codec.PayloadDecoder;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;

/**
 * 获取日志解码器
 */
public class GetJournalCodec implements PayloadDecoder, Type {

    @Override
    public Object decode(Header header, final ByteBuf in) throws Exception {
        GetJournal payload = new GetJournal();
        // 8字节偏移量
        payload.setOffset(in.readLong());
        // 8字节最大偏移量
        payload.setMaxOffset(in.readLong());
        // 4字节拉取等待时间
        if (in.isReadable()) {
            payload.setPullTimeout(in.readInt());
        }
        return payload;
    }

    @Override
    public int type() {
        return Joyqueue0CommandType.GET_JOURNAL.getCode();
    }
}