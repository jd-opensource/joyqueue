package com.jd.journalq.broker.network.protocol.support;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.network.protocol.ProtocolHandlerPipelineFactory;
import com.jd.journalq.broker.network.protocol.ProtocolContext;
import com.jd.journalq.broker.network.protocol.ProtocolManager;
import com.jd.journalq.broker.network.protocol.ProtocolResolver;
import com.jd.journalq.common.network.protocol.ProtocolService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;

import java.util.Map;

/**
 * 多协议处理器管道
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
@ChannelHandler.Sharable
public class DefaultMultiProtocolHandlerPipeline extends ChannelInitializer {

    private ProtocolManager protocolManager;
    private ProtocolHandlerPipelineFactory protocolHandlerPipelineFactory;
    private Map<String /** protocol **/, ProtocolContext> protocolContextMapper;

    public DefaultMultiProtocolHandlerPipeline(ProtocolManager protocolManager, ProtocolHandlerPipelineFactory protocolHandlerPipelineFactory) {
        this.protocolManager = protocolManager;
        this.protocolHandlerPipelineFactory = protocolHandlerPipelineFactory;
        this.protocolContextMapper = initProtocolContextMapper();
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline()
                .addLast(new ProtocolResolver(protocolManager, protocolContextMapper));
    }

    protected ProtocolContext newProtocolContext(ProtocolService protocol) {
        ChannelHandler handlerPipeline = protocolHandlerPipelineFactory.createPipeline(protocol);
        return new ProtocolContext(protocol, handlerPipeline);
    }

    protected Map<String, ProtocolContext> initProtocolContextMapper() {
        Map<String, ProtocolContext> result = Maps.newHashMap();
        for (ProtocolService protocol : protocolManager.getProtocolServices()) {
            ProtocolContext protocolContext = newProtocolContext(protocol);
            result.put(protocol.type(), protocolContext);
        }
        return result;
    }
}