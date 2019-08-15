package io.chubao.joyqueue.broker.kafka.network;

import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Payload;

/**
 * KafkaPayloadCodec
 *
 * author: gaohaoxiang
 * date: 2019/2/28
 */
public interface KafkaPayloadCodec<T extends Payload> extends PayloadCodec<KafkaHeader, T> {
}
