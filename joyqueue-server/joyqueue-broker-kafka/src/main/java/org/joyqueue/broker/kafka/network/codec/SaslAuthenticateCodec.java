package org.joyqueue.broker.kafka.network.codec;

import io.netty.buffer.ByteBuf;
import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.command.SaslAuthenticateRequest;
import org.joyqueue.broker.kafka.command.SaslAuthenticateResponse;
import org.joyqueue.broker.kafka.network.KafkaHeader;
import org.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Type;

/**
 * SaslAuthenticateCodec
 * author: gaohaoxiang
 * date: 2020/4/9
 */
public class SaslAuthenticateCodec implements KafkaPayloadCodec<SaslAuthenticateResponse>, Type {

    private static final byte AUTH_SEP = 0;

    @Override
    public SaslAuthenticateRequest decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        int length = buffer.readInt();
        byte[] authBytes = new byte[length];
        buffer.readBytes(authBytes);

        SaslAuthenticateRequest request = new SaslAuthenticateRequest();
        request.setAuthBytes(authBytes);
        request.setData(parseData(authBytes));
        return request;
    }

    protected SaslAuthenticateRequest.SaslAuthenticateData parseData(byte[] authBytes) {
        StringBuilder buffer = new StringBuilder();
        String app = null;
        String token = null;

        for (int i = 0; i < authBytes.length; i++) {
            byte current = authBytes[i];
            if (current == AUTH_SEP) {
                if (i != 0) {
                    app = buffer.toString();
                    buffer.delete(0, buffer.length());
                }
            } else {
                buffer.append((char) current);
                if (i == authBytes.length - 1) {
                    token = buffer.toString();
                }
            }
        }

        return new SaslAuthenticateRequest.SaslAuthenticateData(app, token);
    }

    @Override
    public void encode(SaslAuthenticateResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getErrorCode());
        Serializer.write(payload.getErrorMessage(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(payload.getAuthBytes().length);
        buffer.writeBytes(payload.getAuthBytes());

        if (payload.getVersion() >= 1) {
            buffer.writeLong(payload.getSessionLifeTimeMs());
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.SASL_AUTHENTICATE.getCode();
    }
}