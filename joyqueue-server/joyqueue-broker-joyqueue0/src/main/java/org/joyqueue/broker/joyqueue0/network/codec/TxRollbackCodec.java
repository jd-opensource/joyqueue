package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.TxRollback;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0PayloadCodec;
import org.joyqueue.broker.joyqueue0.util.Serializer;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.session.TransactionId;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;

/**
 * 分布式事务回滚解码器
 */
public class TxRollbackCodec implements Joyqueue0PayloadCodec, Type {

    @Override
    public Object decode(Header header, ByteBuf in) throws Exception {
        TxRollback payload = new TxRollback();
        // 1字节长度事务ID
        payload.setTransactionId(new TransactionId(Serializer.readString(in)));
        return payload;
    }

    @Override
    public void encode(Payload payload, ByteBuf buffer) {

    }

    @Override
    public int type() {
        return Joyqueue0CommandType.ROLLBACK.getCode();
    }
}