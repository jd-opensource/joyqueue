package io.chubao.joyqueue.broker.network.protocol;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.network.protocol.ProtocolServer;
import io.chubao.joyqueue.network.transport.TransportServer;
import io.chubao.joyqueue.network.transport.config.ServerConfig;
import io.chubao.joyqueue.network.transport.support.ChannelTransportServer;
import io.chubao.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * MultiProtocolTransportServer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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