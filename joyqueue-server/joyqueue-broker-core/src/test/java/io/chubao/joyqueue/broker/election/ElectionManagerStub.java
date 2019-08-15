package io.chubao.joyqueue.broker.election;

import io.chubao.joyqueue.broker.consumer.Consume;
import io.chubao.joyqueue.broker.election.network.codec.*;
import io.chubao.joyqueue.broker.network.codec.BrokerPayloadCodecRegistrar;
import io.chubao.joyqueue.network.transport.TransportServer;
import io.chubao.joyqueue.network.transport.codec.*;
import io.chubao.joyqueue.network.transport.codec.support.DefaultCodec;
import io.chubao.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import io.chubao.joyqueue.network.transport.support.DefaultTransportServerFactory;
import io.chubao.joyqueue.store.StoreService;

/**
 * Created by zhuduohui on 2018/10/8.
 */
public class ElectionManagerStub extends ElectionManager {
    private TransportServer transportServer;
    private ServerConfigStub serverConfig;

    public ElectionManagerStub(ElectionConfig electionConfig, StoreService storeService, Consume consume) {
        super(electionConfig, storeService, consume, new ClusterManagerStub(), new BrokerMonitorStub());
    }

    @Override
    public void doStart() throws Exception {
        super.doStart();

        DefaultCommandHandlerFactory commandHandlerFactory = new DefaultCommandHandlerFactory();
        ElectionCommandHandlerRegistrarStub.register(this, commandHandlerFactory);

        Codec codec = new CodecFactory() {
            @Override
            public Codec getCodec() {
                JoyQueueHeaderCodec headerCodec = new JoyQueueHeaderCodec();
                PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
                new BrokerPayloadCodecRegistrar().register(payloadCodecFactory);
                payloadCodecFactory.register(new VoteRequestDecoder());
                payloadCodecFactory.register(new VoteRequestEncoder());
                payloadCodecFactory.register(new VoteResponseDecoder());
                payloadCodecFactory.register(new VoteResponseEncoder());
                payloadCodecFactory.register(new AppendEntriesRequestDecoder());
                payloadCodecFactory.register(new AppendEntriesRequestEncoder());
                payloadCodecFactory.register(new AppendEntriesResponseDecoder());
                payloadCodecFactory.register(new AppendEntriesResponseEncoder());
                payloadCodecFactory.register(new TimeoutNowRequestDecoder());
                payloadCodecFactory.register(new TimeoutNowRequestEncoder());
                payloadCodecFactory.register(new TimeoutNowResponseDecoder());
                payloadCodecFactory.register(new TimeoutNowResponseEncoder());
                payloadCodecFactory.register(new ReplicateConsumePosRequestDecoder());
                payloadCodecFactory.register(new ReplicateConsumePosRequestEncoder());
                payloadCodecFactory.register(new ReplicateConsumePosResponseDecoder());
                payloadCodecFactory.register(new ReplicateConsumePosResponseEncoder());
                return new DefaultCodec(new DefaultDecoder(headerCodec, payloadCodecFactory), new DefaultEncoder(headerCodec, payloadCodecFactory));
            }
        }.getCodec();
        serverConfig = new ServerConfigStub();
        serverConfig.setPort(electionConfig.getListenPort());
        try {
            transportServer = new DefaultTransportServerFactory(codec, commandHandlerFactory).bind(serverConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        transportServer.start();
    }

    @Override
    public void doStop() {
        transportServer.stop();
        super.doStop();
    }
}
