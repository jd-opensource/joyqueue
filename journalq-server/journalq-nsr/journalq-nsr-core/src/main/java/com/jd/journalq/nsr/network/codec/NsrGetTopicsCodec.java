package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.network.codec.GetTopicsCodec;
import com.jd.journalq.network.command.GetTopics;

import com.jd.journalq.network.transport.command.Types;
import com.jd.journalq.nsr.network.NsrPayloadCodec;

import com.jd.journalq.nsr.network.command.NsrCommandType;

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
