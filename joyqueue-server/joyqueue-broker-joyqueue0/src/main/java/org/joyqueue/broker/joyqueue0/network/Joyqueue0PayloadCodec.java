package org.joyqueue.broker.joyqueue0.network;

import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Payload;

/**
 * JMQ2PayloadCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/28
 */
public interface Joyqueue0PayloadCodec<T extends Payload> extends PayloadCodec<Joyqueue0Header, T> {
}