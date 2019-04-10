package com.jd.journalq.broker.network.backend;

import com.jd.journalq.broker.network.BrokerCommandHandlerFactory;
import com.jd.journalq.broker.network.support.BrokerTransportServerFactory;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.network.event.TransportEvent;
import com.jd.journalq.network.transport.TransportServer;
import com.jd.journalq.network.transport.TransportServerFactory;
import com.jd.journalq.network.transport.command.handler.ExceptionHandler;
import com.jd.journalq.network.transport.config.ServerConfig;
import com.jd.journalq.toolkit.concurrent.EventBus;
import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 后端服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/17
 */
public class BackendServer extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(BackendServer.class);

    private ServerConfig config;
    private EventBus<TransportEvent> transportEventBus;
    private ExceptionHandler exceptionHandler;
    private TransportServerFactory transportServerFactory;
    private TransportServer transportServer;
    private BrokerCommandHandlerFactory commandHandlerFactory;

    public BackendServer(ServerConfig config, BrokerContext brokerContext) {
        this.config = config;
        this.transportEventBus = new EventBus<>("journalq-backend-eventBus");
        this.exceptionHandler = new BrokerExceptionHandler();
        this.commandHandlerFactory = new BrokerCommandHandlerFactory(brokerContext);
        this.transportServerFactory = new BrokerTransportServerFactory(commandHandlerFactory, exceptionHandler, transportEventBus);
    }

    public void addListener(EventListener<TransportEvent> listener) {
        transportEventBus.addListener(listener);
    }

    public void removeListener(EventListener<TransportEvent> listener) {
        transportEventBus.removeListener(listener);
    }

    @Override
    protected void doStart() throws Exception {
        this.transportEventBus.start();
        this.transportServer = transportServerFactory.bind(config, config.getHost(), config.getPort());
        this.transportServer.start();
        logger.info("backend server is started, host: {}, port: {}", config.getHost(), config.getPort());
    }

    @Override
    protected void doStop() {
        this.transportEventBus.stop();
        this.transportServer.stop();
    }
}