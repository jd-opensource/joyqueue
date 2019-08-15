package io.chubao.joyqueue.network.transport.codec;

import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

/**
 * 对象编码
 * Created by hexiaofeng on 16-6-23.
 */
public interface Encoder {

    /**
     * 编码
     *
     * @param obj 对象
     * @param buffer 输出流
     * @throws TransportException.CodecException
     */
    void encode(Object obj, ByteBuf buffer) throws TransportException.CodecException;

}
