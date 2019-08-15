package io.chubao.joyqueue.broker.protocol.network.helper;

import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeaderCodec;
import io.netty.buffer.ByteBuf;

/**
 * JoyQueueProtocolHelper
 *
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public class JoyQueueProtocolHelper {

    public static boolean isSupport(ByteBuf buffer) {
        if (buffer.readableBytes() < JoyQueueHeaderCodec.HEADER_LENGTH) {
            return false;
        }

        int size = buffer.readInt();
        int magic = buffer.readInt();
        byte version = buffer.readByte();
        byte identity = buffer.readByte();
        int requestId = buffer.readInt();
        byte type = buffer.readByte();

        return (size >= JoyQueueHeaderCodec.HEADER_LENGTH
                && magic == JoyQueueHeader.MAGIC
                && version >= 0
                && identity >= 0
                && requestId >= 0
                && JoyQueueCommandType.contains(type));
    }
}