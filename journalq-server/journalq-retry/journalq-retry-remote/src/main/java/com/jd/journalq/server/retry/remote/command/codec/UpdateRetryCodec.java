package com.jd.journalq.server.retry.remote.command.codec;

import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.server.retry.remote.command.UpdateRetry;
import io.netty.buffer.ByteBuf;

/**
 * Created by chengzhiliang on 2018/9/17.
 */
public class UpdateRetryCodec implements PayloadCodec<JMQHeader, UpdateRetry>, Type {

    @Override
    public Object decode(JMQHeader header, ByteBuf buffer) throws Exception {
        if (buffer == null) {
            return null;
        }
        String topic = Serializer.readString(buffer);
        String app = Serializer.readString(buffer);
        byte updateType = buffer.readByte();
        short count = buffer.readShort();
        if (count < 0) {
            count = 0;
        }
        Long[] messages = new Long[count];
        for (int i = 0; i < count; i++) {
            messages[i] = buffer.readLong();
        }
        UpdateRetry updateRetryPayload = new UpdateRetry().topic(topic).app(app).updateType(updateType)
                .updateType(updateType).messages(messages);

        return updateRetryPayload;
    }

    @Override
    public void encode(UpdateRetry payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic(), buffer);
        Serializer.write(payload.getApp(), buffer);
        buffer.writeByte(payload.getUpdateType());
        Long[] messages = payload.getMessages();
        int count = messages == null ? 0 : messages.length;
        buffer.writeShort(count);
        if (count > 0) {
            for (int i = 0; i < messages.length; i++) {
                buffer.writeLong(messages[i]);
            }
        }
    }

    @Override
    public int type() {
        return CommandType.UPDATE_RETRY;
    }
}
