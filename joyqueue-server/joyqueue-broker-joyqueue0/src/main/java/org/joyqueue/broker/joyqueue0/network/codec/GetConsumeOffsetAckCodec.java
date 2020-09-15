package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;

/**
 * 获取消费位置应答解码器
 */
public class GetConsumeOffsetAckCodec extends GetConsumeOffsetCodec {

    @Override
    public int type() {
        return Joyqueue0CommandType.GET_CONSUMER_OFFSET_ACK.getCode();
    }
}