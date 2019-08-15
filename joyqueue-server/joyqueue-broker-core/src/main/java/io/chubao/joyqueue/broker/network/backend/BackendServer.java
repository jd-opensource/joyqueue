package io.chubao.joyqueue.broker.network.backend;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.network.BrokerCommandHandlerFactory;
import io.chubao.joyqueue.broker.network.support.BrokerTransportServerFactory;
import io.chubao.joyqueue.network.event.TransportEvent;
import io.chubao.joyqueue.network.transport.TransportServer;
import io.chubao.joyqueue.network.transport.TransportServerFactory;
import io.chubao.joyqueue.network.transport.command.handler.ExceptionHandler;
import io.chubao.joyqueue.network.transport.config.ServerConfig;
import io.chubao.joyqueue.toolkit.concurrent.EventBus;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import io.chubao.joyqueue.toolkit.service.Service;
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
        this.transportEventBus = new EventBus<>("joyqueue-backend-eventBus");
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