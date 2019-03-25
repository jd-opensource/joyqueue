package com.jd.journalq.broker.network.protocol;

import com.jd.journalq.network.protocol.Protocol;
import io.netty.channel.ChannelHandler;

/**
 * 协议处理管道工厂
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/14
 */
public interface ProtocolHandlerPipelineFactory {

    public ChannelHandler createPipeline(Protocol protocol);
}