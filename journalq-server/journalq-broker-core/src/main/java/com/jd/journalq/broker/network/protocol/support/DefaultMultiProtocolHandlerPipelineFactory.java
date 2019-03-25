package com.jd.journalq.broker.network.protocol.support;

import com.jd.journalq.broker.network.protocol.MultiProtocolHandlerPipelineFactory;
import com.jd.journalq.broker.network.protocol.ProtocolHandlerPipelineFactory;
import com.jd.journalq.broker.network.protocol.ProtocolManager;
import io.netty.channel.ChannelHandler;

/**
 * 默认多协议处理管道工厂
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