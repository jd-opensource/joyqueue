package com.jd.joyqueue.broker.jmq2.network.helper;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.JMQ2Consts;
import com.jd.joyqueue.broker.jmq2.network.protocol.JMQ2HeaderCodec;
import io.netty.buffer.ByteBuf;

/**
 * 协议帮助类
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/3
 */
public class JMQ2ProtocolHelper {

    public static boolean isSupport(ByteBuf buffer) {
        if (buffer.readableBytes() < JMQ2HeaderCodec.HEADER_LENGTH) {
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
                && version >= JMQ2Consts.MIN_SUPPORTED_PROTOCOL_VERSION
                && version <= JMQ2Consts.MAX_SUPPORTED_PROTOCOL_VERSION
                && JMQ2CommandType.contains(type)) {
            return true;
        }

        return false;
    }
}
