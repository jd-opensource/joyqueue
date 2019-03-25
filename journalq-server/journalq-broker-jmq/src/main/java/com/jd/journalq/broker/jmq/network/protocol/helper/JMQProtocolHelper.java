package com.jd.journalq.broker.jmq.network.protocol.helper;

import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.JMQHeaderCodec;
import io.netty.buffer.ByteBuf;

/**
 * JMQProtocolHelper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/28
 */
public class JMQProtocolHelper {

    public static boolean isSupport(ByteBuf buffer) {
        if (buffer.readableBytes() < JMQHeaderCodec.HEADER_LENGTH) {
            return false;
        }

        int size = buffer.readInt();
        int magic = buffer.readInt();
        byte version = buffer.readByte();
        byte identity = buffer.readByte();
        int requestId = buffer.readInt();
        byte type = buffer.readByte();

        return (size >= JMQHeaderCodec.HEADER_LENGTH
                && magic == JMQHeader.MAGIC
                && version >= 0
                && identity >= 0
                && requestId >= 0
                && JMQCommandType.contains(type));
    }
}