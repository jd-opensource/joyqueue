package io.chubao.joyqueue.nsr.message.support.network.transport;

import io.chubao.joyqueue.network.codec.BooleanAckCodec;
import io.chubao.joyqueue.network.transport.TransportClient;
import io.chubao.joyqueue.network.transport.TransportClientFactory;
import io.chubao.joyqueue.network.transport.codec.PayloadCodecFactory;
import io.chubao.joyqueue.network.transport.codec.support.JoyQueueCodec;
import io.chubao.joyqueue.network.transport.config.ClientConfig;
import io.chubao.joyqueue.network.transport.support.DefaultTransportClientFactory;
import io.chubao.joyqueue.nsr.message.support.network.codec.MessengerHeartbeatRequestCodec;
import io.chubao.joyqueue.nsr.message.support.network.codec.MessengerPublishRequestCodec;

/**
 * MessengerTransportClientFactory
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerTransportClientFactory implements TransportClientFactory {

    private DefaultTransportClientFactory transportClientFactory;

    public MessengerTransportClientFactory() {
        PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
        payloadCodecFactory.register(new MessengerPublishRequestCodec());
        payloadCodecFactory.register(new MessengerHeartbeatRequestCodec());
        payloadCodecFactory.register(new BooleanAckCodec());

        this.transportClientFactory = new DefaultTransportClientFactory(new JoyQueueCodec(payloadCodecFactory));
    }

    @Override
    public TransportClient create(ClientConfig config) {
        return transportClientFactory.create(config);
    }
}