package io.chubao.joyqueue.broker.network.protocol;

import io.chubao.joyqueue.network.protocol.Protocol;
import io.netty.channel.ChannelHandler;

/**
 * ProtocolHandlerPipelineFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/14
 */
public interface ProtocolHandlerPipelineFactory {

    ChannelHandler createPipeline(Protocol protocol);
}