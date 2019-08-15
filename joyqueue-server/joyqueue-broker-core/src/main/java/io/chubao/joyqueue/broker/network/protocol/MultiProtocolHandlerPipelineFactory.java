package io.chubao.joyqueue.broker.network.protocol;

import io.netty.channel.ChannelHandler;

/**
 * MultiProtocolHandlerPipelineFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/14
 */
public interface MultiProtocolHandlerPipelineFactory {

    ChannelHandler createPipeline();
}