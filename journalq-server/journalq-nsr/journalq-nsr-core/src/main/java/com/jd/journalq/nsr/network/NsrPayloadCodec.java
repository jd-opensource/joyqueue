package com.jd.journalq.nsr.network;

import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Payload;

/**
 * @author wylixiaobin
 * Date: 2019/3/14
 */
public interface NsrPayloadCodec<T extends Payload> extends PayloadCodec<Header,T> {
}
