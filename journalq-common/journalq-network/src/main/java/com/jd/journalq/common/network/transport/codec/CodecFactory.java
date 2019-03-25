package com.jd.journalq.common.network.transport.codec;

/**
 * 编解码器工厂
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public interface CodecFactory {

    public Codec getCodec();
}