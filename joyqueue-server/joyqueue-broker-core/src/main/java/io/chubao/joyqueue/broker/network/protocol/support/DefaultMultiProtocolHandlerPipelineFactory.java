/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.broker.network.protocol.support;

import io.chubao.joyqueue.broker.network.protocol.MultiProtocolHandlerPipelineFactory;
import io.chubao.joyqueue.broker.network.protocol.ProtocolHandlerPipelineFactory;
import io.chubao.joyqueue.broker.network.protocol.ProtocolManager;
import io.netty.channel.ChannelHandler;

/**
 * DefaultMultiProtocolHandlerPipelineFactory
 *
 * author: gaohaoxiang
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