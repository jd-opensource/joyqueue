package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.AckMessage;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.message.MessageLocation;
import org.joyqueue.network.session.ConsumerId;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ack
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class AckMessageCodec implements JMQ2PayloadCodec, Type {

    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        AckMessage payload = new AckMessage();
        payload.setConsumerId(new ConsumerId(Serializer.readString(buffer, 1)));
        // 6字节服务地址
        // byte[] address = Serializer.readBytes(buffer,temp 6);
        // 6字节服务地址被占用存储partition值（jmq2协议queueId为一个字节，不够存储short类型的partition）
        byte[] temp = Serializer.readBytes(buffer, 4); // 前四位为空
        short partition = buffer.readShort();
        // 1字节主题长度
        String topic = Serializer.readString(buffer, 1);
        // 2字节条数
        int length = buffer.readUnsignedShort();
        MessageLocation[] locations = new MessageLocation[length];
        for (int i = 0; i < length; i++) {
            // 1字节队列ID
            short queueId = buffer.readUnsignedByte(); // jmq4的partition是short类型，1个字节不够存储，用serverAddress临时代替
            // 8字节队列偏移
            long queueOffset = buffer.readLong();
            // 8字节日志偏移
            long journalOffset = buffer.readLong();

            locations[i] = new MessageLocation(topic, partition, queueOffset);
        }

        payload.setLocations(locations);
        return payload;
    }

    @Override
    public void encode(Payload payload, ByteBuf buffer) {

    }

    @Override
    public int type() {
        return JMQ2CommandType.ACK_MESSAGE.getCode();
    }
}