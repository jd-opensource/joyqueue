package com.jd.journalq.broker.network.frontend;

import com.jd.journalq.common.network.transport.command.handler.ExceptionHandler;
import com.jd.journalq.common.network.event.TransportEvent;
import com.jd.journalq.common.network.transport.TransportServer;
import com.jd.journalq.common.network.transport.TransportServerFactory;
import com.jd.journalq.common.network.transport.config.ServerConfig;
import com.jd.journalq.broker.network.backend.BrokerExceptionHandler;
import com.jd.journalq.broker.network.protocol.MultiProtocolTransportServerFactory;
import com.jd.journalq.broker.network.protocol.ProtocolManager;
import com.jd.journalq.toolkit.concurrent.EventBus;
import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 前端服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/17
 */
public class FrontendServer extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(FrontendServer.class);

    private ServerConfig config;
    private ProtocolManager protocolManager;
    private EventBus<TransportEvent> transportEventBus;
    private ExceptionHandler exceptionHandler;
    private TransportServerFactory transportServerFactory;
    private TransportServer transportServer;

    public FrontendServer(ServerConfig config, ProtocolManager protocolManager) {
        this.config = config;
        this.protocolManager = protocolManager;
        this.transportEventBus = new EventBus<>("jmq-frontend-eventBus");
        this.exceptionHandler = new BrokerExceptionHandler();
        this.transportServerFactory = new MultiProtocolTransportServerFactory(protocolManager, transportEventBus, exceptionHandler);
    }

    public void addListener(EventListener<TransportEvent> listener) {
        transportEventBus.addListener(listener);
    }

    public void removeListener(EventListener<TransportEvent> listener) {
        transportEventBus.removeListener(listener);
    }

    @Override
    protected void doStart() throws Exception {
        transportEventBus.start();
        transportServer = transportServerFactory.bind(config, config.getHost(), config.getPort());
        transportServer.start();
        logger.info("frontend server is started, host: {}, port: {}", config.getHost(), config.getPort());
    }

    @Override
    protected void doStop() {
        transportEventBus.stop();
        transportServer.stop();
    }
}