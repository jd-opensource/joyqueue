package com.jd.journalq.server.retry.remote.command.codec;

import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.server.retry.model.RetryMessageModel;
import com.jd.journalq.server.retry.remote.command.PutRetry;
import com.jd.journalq.server.retry.util.RetrySerializerUtil;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 添加重试消息编解码
 *
 * Created by chengzhiliang on 2018/9/17.
 */
public class PutRetryCodec implements PayloadCodec<JMQHeader, PutRetry>, Type {

    @Override
    public Object decode(JMQHeader header, ByteBuf buffer) throws Exception {
        if (buffer == null) {
            return null;
        }

        int count = buffer.readShort(); // 读取重试消息条数
        List<RetryMessageModel> messageList = new ArrayList<>(count);

        ByteBuffer byteBuffer = buffer.nioBuffer();
        for (int i = 0; i < count; i++) {
            RetryMessageModel retryMessageModel = RetrySerializerUtil.deserialize(byteBuffer);
            messageList.add(retryMessageModel);
        }

        PutRetry putRetry = new PutRetry(messageList);
        return putRetry;
    }

    @Override
    public void encode(PutRetry payload, ByteBuf buffer) throws Exception {
        List<RetryMessageModel> messageList = payload.getMessages();
        // 2字节条数
        if (messageList == null) {
            buffer.writeShort(0);
            return;
        }
        int size = messageList.size();
        buffer.writeShort(size);

        for (RetryMessageModel message : messageList) {
            ByteBuffer srcBuffer = RetrySerializerUtil.serialize(message);
            buffer.writeBytes(srcBuffer);
        }
    }

    @Override
    public int type() {
        return CommandType.PUT_RETRY;
    }
}
