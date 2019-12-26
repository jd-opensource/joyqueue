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
package org.joyqueue.broker.network.protocol;

import com.google.common.collect.Lists;
import org.joyqueue.network.protocol.ProtocolServer;
import org.joyqueue.network.transport.TransportServer;
import org.joyqueue.network.transport.config.ServerConfig;
import org.joyqueue.network.transport.support.ChannelTransportServer;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * MultiProtocolTransportServer
 *
 * author: gaohaoxiang
 * date: 2018/8/13
 */
public class MultiProtocolTransportServer extends Service implements TransportServer {

    protected static final Logger logger = LoggerFactory.getLogger(MultiProtocolTransportServer.class);

    private ServerConfig serverConfig;
    private String host;
    private int port;
    private ProtocolManager protocolManager;
    private MultiProtocolHandlerPipelineFactory multiProtocolHandlerPipelineFactory;
    private ProtocolHandlerPipelineFactory protocolHandlerPipelineFactory;

    private TransportServer protocolServiceServer;
    private List<ProtocolContext> protocolServers;

    public MultiProtocolTransportServer(ServerConfig serverConfig, String host,
                                        int port, ProtocolManager protocolManager,
                                        MultiProtocolHandlerPipelineFactory multiProtocolHandlerPipelineFactory,
                                        ProtocolHandlerPipelineFactory protocolHandlerPipelineFactory) {
        this.serverConfig = serverConfig;
        this.host = host;
        this.port = port;
        this.protocolManager = protocolManager;
        this.multiProtocolHandlerPipelineFactory = multiProtocolHandlerPipelineFactory;
        this.protocolHandlerPipelineFactory = protocolHandlerPipelineFactory;
    }

    @Override
    public InetSocketAddress getSocketAddress() {
        return new InetSocketAddress(host, port);
    }

    @Override
    public boolean isSSLServer() {
        return false;
    }

    @Override
    protected void validate() throws Exception {
        this.protocolServiceServer = new ChannelTransportServer(multiProtocolHandlerPipelineFactory.createPipeline(), serverConfig, host, port);
        this.protocolServers = initProtocolServers();
    }

    @Override
    protected void doStart() throws Exception {
        protocolServiceServer.start();
        for (ProtocolContext protocolServer : protocolServers) {
            try {
                protocolServer.getTransportServer().start();
                logger.info("protocol {} is start, address: {}", protocolServer.getProtocol().type(), protocolServer.getTransportServer().getSocketAddress());
            } catch (Exception e) {
                logger.error("protocol {} start failed", protocolServer.getProtocol().type(), e);
            }
        }
    }

    @Override
    protected void doStop() {
        protocolServiceServer.stop();
        for (ProtocolContext protocolServer : protocolServers) {
            try {
                protocolServer.getTransportServer().stop();
                logger.info("protocol {} is stop", protocolServer.getProtocol().type(), protocolServer.getTransportServer().getSocketAddress());
            } catch (Exception e) {
                logger.error("protocol {} stop failed", protocolServer.getProtocol().type(), e);
            }
        }
    }

    protected List<ProtocolContext> initProtocolServers() {
        List<ProtocolContext> result = Lists.newArrayList();
        for (ProtocolServer protocolServer : protocolManager.getProtocolServers()) {
            ServerConfig protocolServerConfig = protocolServer.createServerConfig(serverConfig);
            TransportServer transportServer = new ChannelTransportServer(protocolHandlerPipelineFactory.createPipeline(protocolServer),
                    protocolServerConfig, protocolServerConfig.getHost(), protocolServerConfig.getPort());
            result.add(new ProtocolContext(protocolServer, transportServer));
        }
        return result;
    }
}