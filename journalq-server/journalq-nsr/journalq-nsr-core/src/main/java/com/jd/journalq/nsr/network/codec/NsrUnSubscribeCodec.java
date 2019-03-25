package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.network.codec.UnSubscribeCodec;
import com.jd.journalq.network.command.UnSubscribe;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.NsrCommandType;

/**
 * @author wylixiaobin
 * Date: 2019/3/20
 */
public class NsrUnSubscribeCodec extends UnSubscribeCodec implements NsrPayloadCodec<UnSubscribe> {
    @Override
    public int type() {
        return NsrCommandType.UN_SUBSCRIBE;
    }
}
