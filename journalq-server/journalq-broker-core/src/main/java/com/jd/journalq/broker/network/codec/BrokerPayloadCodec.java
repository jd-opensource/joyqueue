package com.jd.journalq.broker.network.codec;

import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Payload;

/**
 * BrokerPayloadCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/28
 */
public interface BrokerPayloadCodec<T extends Payload> extends PayloadCodec<JMQHeader, T> {
}