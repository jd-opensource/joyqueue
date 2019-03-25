package com.jd.journalq.broker.jmq.network;

import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Payload;

/**
 * JMQPayloadCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/28
 */
public interface JMQPayloadCodec<T extends Payload> extends PayloadCodec<JMQHeader, T> {
}