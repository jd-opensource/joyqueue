package com.jd.journalq.network.transport.codec;

import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Payload;

/**
 * jmq消息体编解码器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public interface PayloadCodec<H extends Header, T extends Payload> extends PayloadDecoder<H>, PayloadEncoder<T> {
}