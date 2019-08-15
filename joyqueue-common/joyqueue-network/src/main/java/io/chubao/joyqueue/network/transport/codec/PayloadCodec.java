package io.chubao.joyqueue.network.transport.codec;

import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Payload;

/**
 * PayloadCodec
 *
 * author: gaohaoxiang
 * date: 2018/8/21
 */
public interface PayloadCodec<H extends Header, T extends Payload> extends PayloadDecoder<H>, PayloadEncoder<T> {
}