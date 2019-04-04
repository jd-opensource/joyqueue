package com.jd.journalq.broker.kafka.network.codec;

import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.command.EndTxnResponse;
import com.jd.journalq.broker.kafka.network.KafkaHeader;
import com.jd.journalq.broker.kafka.network.KafkaPayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * EndTxnCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
// TODO
public class EndTxnCodec implements KafkaPayloadCodec<EndTxnResponse>, Type {

    @Override
    public Object decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        return null;
    }

    @Override
    public void encode(EndTxnResponse payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public int type() {
        return KafkaCommandType.END_TXN.getCode();
    }
}