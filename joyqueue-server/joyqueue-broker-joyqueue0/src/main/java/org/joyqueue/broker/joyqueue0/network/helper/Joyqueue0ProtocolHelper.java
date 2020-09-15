package org.joyqueue.broker.joyqueue0.network.helper;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.Joyqueue0Consts;
import org.joyqueue.broker.joyqueue0.network.protocol.Joyqueue0HeaderCodec;
import io.netty.buffer.ByteBuf;

/**
 * 协议帮助类
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/3
 */
public class Joyqueue0ProtocolHelper {

    public static boolean isSupport(ByteBuf buffer) {
        if (buffer.readableBytes() < Joyqueue0HeaderCodec.HEADER_LENGTH) {
            return false;
        }

        // 消息大小
        int size = buffer.readInt();
        // 版本
        byte version = buffer.readByte();
        // flag标记
        byte flag = buffer.readByte();
        // 请求ID
        int requestId = buffer.readInt();
        // 命令类型
        int type = buffer.readByte();

        if (size > 0
                && version >= Joyqueue0Consts.MIN_SUPPORTED_PROTOCOL_VERSION
                && version <= Joyqueue0Consts.MAX_SUPPORTED_PROTOCOL_VERSION
                && Joyqueue0CommandType.contains(type)) {
            return true;
        }

        return false;
    }
}
