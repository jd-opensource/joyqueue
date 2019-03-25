package com.jd.journalq.network.protocol;

import io.netty.buffer.ByteBuf;

/**
 * 协议服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/25
 */
public interface ProtocolService extends Protocol {

    boolean isSupport(ByteBuf buffer);
}