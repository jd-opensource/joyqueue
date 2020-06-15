package com.jd.joyqueue.broker.jmq2.network;

import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Payload;

/**
 * JMQ2PayloadCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/28
 */
public interface JMQ2PayloadCodec<T extends Payload> extends PayloadCodec<JMQ2Header, T> {
}