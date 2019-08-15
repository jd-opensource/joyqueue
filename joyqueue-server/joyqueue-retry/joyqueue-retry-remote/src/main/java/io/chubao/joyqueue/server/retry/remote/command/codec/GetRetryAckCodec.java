package io.chubao.joyqueue.server.retry.remote.command.codec;

import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.server.retry.model.RetryMessageModel;
import io.chubao.joyqueue.server.retry.remote.command.GetRetryAck;
import io.chubao.joyqueue.server.retry.util.RetrySerializerUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengzhiliang on 2018/9/17.
 */
public class GetRetryAckCodec implements PayloadCodec<JoyQueueHeader, GetRetryAck>, Type {
    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        if (buffer == null) {
            return null;
        }

        GetRetryAck getRetryAckPayload = new GetRetryAck();

        int count = buffer.readShort(); // 读取重试消息条数
        List<RetryMessageModel> list = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            RetryMessageModel retryMessageModel = RetrySerializerUtil.deserialize(buffer);
            list.add(retryMessageModel);
        }

        getRetryAckPayload.setMessages(list);
        return getRetryAckPayload;
    }

    @Override
    public void encode(GetRetryAck payload, ByteBuf buffer) throws Exception {
        List<RetryMessageModel> messages = payload.getMessages();
        // 2字节条数
        if (messages == null) {
            buffer.writeShort(0);
            return;
        }
        int size = messages.size();
        buffer.writeShort(size);

        for (RetryMessageModel message : messages) {
            ByteBuffer srcBuffer = RetrySerializerUtil.serialize(message);
            buffer.writeBytes(srcBuffer);
        }
    }

    @Override
    public int type() {
        return CommandType.GET_RETRY_ACK;
    }
}
