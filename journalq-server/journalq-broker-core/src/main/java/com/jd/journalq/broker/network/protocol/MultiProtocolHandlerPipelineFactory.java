package com.jd.journalq.broker.network.protocol;

import io.netty.channel.ChannelHandler;

/**
 * 多协议处理管道工厂
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/14
 */
public interface MultiProtocolHandlerPipelineFactory {

    public ChannelHandler createPipeline();
}