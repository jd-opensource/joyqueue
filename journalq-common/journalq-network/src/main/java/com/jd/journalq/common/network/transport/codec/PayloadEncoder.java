package com.jd.journalq.common.network.transport.codec;

import com.jd.journalq.common.network.transport.command.Payload;
import io.netty.buffer.ByteBuf;

/**
 * jmq消息体编码器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public interface PayloadEncoder<T extends Payload> {

    public void encode(T payload, ByteBuf buffer) throws Exception;
}