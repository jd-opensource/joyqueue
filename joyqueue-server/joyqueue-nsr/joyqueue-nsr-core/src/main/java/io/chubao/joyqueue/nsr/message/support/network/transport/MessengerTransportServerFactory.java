package io.chubao.joyqueue.nsr.message.support.network.transport;

import io.chubao.joyqueue.network.codec.BooleanAckCodec;
import io.chubao.joyqueue.network.transport.TransportServer;
import io.chubao.joyqueue.network.transport.TransportServerFactory;
import io.chubao.joyqueue.network.transport.codec.PayloadCodecFactory;
import io.chubao.joyqueue.network.transport.codec.support.JoyQueueCodec;
import io.chubao.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import io.chubao.joyqueue.network.transport.config.ServerConfig;
import io.chubao.joyqueue.network.transport.support.DefaultTransportServerFactory;
import io.chubao.joyqueue.nsr.config.MessengerConfig;
import io.chubao.joyqueue.nsr.message.support.network.codec.MessengerHeartbeatRequestCodec;
import io.chubao.joyqueue.nsr.message.support.network.codec.MessengerPublishRequestCodec;
import io.chubao.joyqueue.nsr.message.support.network.handler.MessengerHeartbeatRequestHandler;
import io.chubao.joyqueue.nsr.message.support.network.handler.MessengerPublishRequestHandler;
import io.chubao.joyqueue.toolkit.concurrent.EventBus;

/**
 * MessengerTransportServerFactory
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerTransportServerFactory implements TransportServerFactory {

    private TransportServerFactory transportServerFactory;
    private MessengerConfig config;
    private EventBus eventBus;

    public MessengerTransportServerFactory(MessengerConfig config, EventBus eventBus) {
        PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
        payloadCodecFactory.register(new MessengerPublishRequestCodec());
        payloadCodecFactory.register(new MessengerHeartbeatRequestCodec());
        payloadCodecFactory.register(new BooleanAckCodec());

        DefaultCommandHandlerFactory commandHandlerFactory = new DefaultCommandHandlerFactory();
        commandHandlerFactory.register(new MessengerPublishRequestHandler(config, eventBus));
        commandHandlerFactory.register(new MessengerHeartbeatRequestHandler());

        this.eventBus = eventBus;
        this.transportServerFactory = new DefaultTransportServerFactory(new JoyQueueCodec(payloadCodecFactory), commandHandlerFactory);
    }

    @Override
    public TransportServer bind(ServerConfig serverConfig) {
        return transportServerFactory.bind(serverConfig);
    }

    @Override
    public TransportServer bind(ServerConfig serverConfig, String host) {
        return transportServerFactory.bind(serverConfig, host);
    }

    @Override
    public TransportServer bind(ServerConfig serverConfig, String host, int port) {
        return transportServerFactory.bind(serverConfig, host, port);
    }
}