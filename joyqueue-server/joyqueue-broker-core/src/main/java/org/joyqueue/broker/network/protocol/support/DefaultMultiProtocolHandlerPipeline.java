/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.network.protocol.support;

import com.google.common.collect.Maps;
import org.joyqueue.broker.network.protocol.ProtocolContext;
import org.joyqueue.broker.network.protocol.ProtocolHandlerPipelineFactory;
import org.joyqueue.broker.network.protocol.ProtocolManager;
import org.joyqueue.broker.network.protocol.ProtocolResolver;
import org.joyqueue.network.protocol.ProtocolService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;

import java.util.Map;

/**
 * DefaultMultiProtocolHandlerPipeline
 *
 * author: gaohaoxiang
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