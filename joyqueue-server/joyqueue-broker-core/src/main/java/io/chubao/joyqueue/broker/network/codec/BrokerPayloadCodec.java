package io.chubao.joyqueue.broker.network.codec;

import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Payload;

/**
 * BrokerPayloadCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/28
 */
public interface BrokerPayloadCodec<T extends Payload> extends PayloadCodec<JoyQueueHeader, T> {
}