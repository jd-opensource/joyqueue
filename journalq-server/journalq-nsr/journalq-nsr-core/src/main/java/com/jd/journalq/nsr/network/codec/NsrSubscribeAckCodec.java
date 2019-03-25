package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.network.codec.SubscribeAckCodec;
import com.jd.journalq.network.command.SubscribeAck;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.NsrCommandType;

/**
 * @author wylixiaobin
 * Date: 2019/3/20
 */
public class NsrSubscribeAckCodec extends SubscribeAckCodec implements NsrPayloadCodec<SubscribeAck> {
    @Override
    public int type() {
        return NsrCommandType.SUBSCRIBE_ACK;
    }
}
