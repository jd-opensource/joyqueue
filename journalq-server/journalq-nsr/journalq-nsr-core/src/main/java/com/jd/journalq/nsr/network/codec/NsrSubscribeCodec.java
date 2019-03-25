package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.network.codec.SubscribeCodec;
import com.jd.journalq.network.command.Subscribe;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.NsrCommandType;


/**
 * @author wylixiaobin
 * Date: 2019/3/20
 */
public class NsrSubscribeCodec extends SubscribeCodec implements NsrPayloadCodec<Subscribe> {
    @Override
    public int type() {
        return NsrCommandType.SUBSCRIBE;
    }
}
