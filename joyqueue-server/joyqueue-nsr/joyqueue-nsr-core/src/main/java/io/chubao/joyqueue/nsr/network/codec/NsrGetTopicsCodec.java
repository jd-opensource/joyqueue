package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.codec.GetTopicsCodec;
import io.chubao.joyqueue.network.command.GetTopics;

import io.chubao.joyqueue.network.transport.command.Types;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;

import io.chubao.joyqueue.nsr.network.command.NsrCommandType;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class NsrGetTopicsCodec extends GetTopicsCodec implements NsrPayloadCodec<GetTopics>, Types {

    @Override
    public int[] types() {
        return new int[]{NsrCommandType.GET_TOPICS,NsrCommandType.MQTT_GET_TOPICS};
    }
}
