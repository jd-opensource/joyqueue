package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.codec.GetTopicsAckCodec;
import io.chubao.joyqueue.network.command.GetTopicsAck;
import io.chubao.joyqueue.network.transport.command.Types;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;


/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class NsrGetTopicsAckCodec extends GetTopicsAckCodec implements NsrPayloadCodec<GetTopicsAck>, Types {

    @Override
    public int[] types() {
        return new int[]{NsrCommandType.GET_TOPICS_ACK,NsrCommandType.MQTT_GET_TOPICS_ACK};
    }
}
