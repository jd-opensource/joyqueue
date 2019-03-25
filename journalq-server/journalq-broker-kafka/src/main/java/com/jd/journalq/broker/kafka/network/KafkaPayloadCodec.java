package com.jd.journalq.broker.kafka.network;

import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Payload;

/**
 * KafkaPayloadCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/28
 */
public interface KafkaPayloadCodec<T extends Payload> extends PayloadCodec<KafkaHeader, T> {
}
