package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.common.network.codec.NullPayloadCodec;
import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.NsrCommandType;

/**
 * @author wylixiaobin
 * Date: 2019/2/14
 */
public class NsrNullPayLoadCodec extends NullPayloadCodec implements NsrPayloadCodec {
    @Override
    public int[] types() {
        return new int[]{CommandType.BOOLEAN_ACK,
                NsrCommandType.GET_ALL_BROKERS,
                NsrCommandType.GET_ALL_TOPICS,
                NsrCommandType.GET_ALL_CONFIG,
                NsrCommandType.PUSH_NAMESERVER_EVENT_ACK,
                NsrCommandType.LEADER_REPORT_ACK};
    }
}
