package io.chubao.joyqueue.broker.network.protocol.support;

import io.chubao.joyqueue.broker.network.protocol.MultiProtocolHandlerPipelineFactory;
import io.chubao.joyqueue.broker.network.protocol.ProtocolHandlerPipelineFactory;
import io.chubao.joyqueue.broker.network.protocol.ProtocolManager;
import io.netty.channel.ChannelHandler;

/**
 * DefaultMultiProtocolHandlerPipelineFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/14
 */
public class DefaultMultiProtocolHandlerPipelineFactory implements MultiProtocolHandlerPipelineFactory {

    private ProtocolManager protocolManager;
    private ProtocolHandlerPipelineFactory protocolHandlerPipelineFactory;

    public DefaultMultiProtocolHandlerPipelineFactory(ProtocolManager protocolManager, ProtocolHandlerPipelineFactory protocolHandlerPipelineFactory) {
        this.protocolManager = protocolManager;
        this.protocolHandlerPipelineFactory = protocolHandlerPipelineFactory;
    }

    @Override
    public ChannelHandler createPipeline() {
        return new DefaultMultiProtocolHandlerPipeline(protocolManager, protocolHandlerPipelineFactory);
    }
}