package io.chubao.joyqueue.nsr.network;

import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Payload;

/**
 * @author wylixiaobin
 * Date: 2019/3/14
 */
public interface NsrPayloadCodec<T extends Payload> extends PayloadCodec<Header,T> {
}
