package io.chubao.joyqueue.broker.kafka.network.helper;

import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.netty.buffer.ByteBuf;

/**
 * KafkaProtocolHelper
 *
 * author: gaohaoxiang
 * date: 2018/11/13
 */
public class KafkaProtocolHelper {

    public static boolean isSupport(ByteBuf buffer) {
        if (buffer.readableBytes() < 12) {
            return false;
        }
        int size = buffer.readInt();
        short type = buffer.readShort();
        short version = buffer.readShort();
//        if (size > 0 && (type >= 0 && type < 255) && version <= 3) {
        if (size > 0
                && version >= 0
                && KafkaCommandType.contains(type)) {
            return true;
        }
        return false;
    }
}