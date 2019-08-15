package io.chubao.joyqueue.network.protocol;

import io.netty.buffer.ByteBuf;

/**
 * ProtocolService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/25
 */
public interface ProtocolService extends Protocol {

    boolean isSupport(ByteBuf buffer);
}