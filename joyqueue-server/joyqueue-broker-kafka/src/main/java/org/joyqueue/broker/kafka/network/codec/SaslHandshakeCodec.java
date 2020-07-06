package org.joyqueue.broker.kafka.network.codec;

import io.netty.buffer.ByteBuf;
import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.command.SaslHandshakeRequest;
import org.joyqueue.broker.kafka.command.SaslHandshakeResponse;
import org.joyqueue.broker.kafka.network.KafkaHeader;
import org.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Type;

/**
 * SaslHandshakeCodec
 * author: gaohaoxiang
 * date: 2020/4/9
 */
public class SaslHandshakeCodec implements KafkaPayloadCodec<SaslHandshakeResponse>, Type {

    @Override
    public SaslHandshakeRequest decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        String mechanism = Serializer.readString(buffer, Serializer.SHORT_SIZE);

        SaslHandshakeRequest request = new SaslHandshakeRequest();
        request.setMechanism(mechanism);
        return request;
    }

    @Override
    public void encode(SaslHandshakeResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getErrorCode());
        buffer.writeInt(payload.getMechanisms().size());
        for (String mechanism : payload.getMechanisms()) {
            Serializer.write(mechanism, buffer, Serializer.SHORT_SIZE);
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.SASL_HANDSHAKE.getCode();
    }
}