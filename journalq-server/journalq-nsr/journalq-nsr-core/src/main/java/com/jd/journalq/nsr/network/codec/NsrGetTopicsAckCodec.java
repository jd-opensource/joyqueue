package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.network.codec.GetTopicsAckCodec;
import com.jd.journalq.network.command.GetTopicsAck;
import com.jd.journalq.network.transport.command.Types;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.NsrCommandType;


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
