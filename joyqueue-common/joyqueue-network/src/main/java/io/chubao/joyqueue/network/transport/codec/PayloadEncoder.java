package io.chubao.joyqueue.network.transport.codec;

import io.chubao.joyqueue.network.transport.command.Payload;
import io.netty.buffer.ByteBuf;

/**
 * PayloadEncoder
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public interface PayloadEncoder<T extends Payload> {

    void encode(T payload, ByteBuf buffer) throws Exception;
}