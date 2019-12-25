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
package org.joyqueue.broker.election;

import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.election.network.codec.AppendEntriesRequestDecoder;
import org.joyqueue.broker.election.network.codec.AppendEntriesRequestEncoder;
import org.joyqueue.broker.election.network.codec.AppendEntriesResponseDecoder;
import org.joyqueue.broker.election.network.codec.AppendEntriesResponseEncoder;
import org.joyqueue.broker.election.network.codec.ReplicateConsumePosRequestDecoder;
import org.joyqueue.broker.election.network.codec.ReplicateConsumePosRequestEncoder;
import org.joyqueue.broker.election.network.codec.ReplicateConsumePosResponseDecoder;
import org.joyqueue.broker.election.network.codec.ReplicateConsumePosResponseEncoder;
import org.joyqueue.broker.election.network.codec.TimeoutNowRequestDecoder;
import org.joyqueue.broker.election.network.codec.TimeoutNowRequestEncoder;
import org.joyqueue.broker.election.network.codec.TimeoutNowResponseDecoder;
import org.joyqueue.broker.election.network.codec.TimeoutNowResponseEncoder;
import org.joyqueue.broker.election.network.codec.VoteRequestDecoder;
import org.joyqueue.broker.election.network.codec.VoteRequestEncoder;
import org.joyqueue.broker.election.network.codec.VoteResponseDecoder;
import org.joyqueue.broker.election.network.codec.VoteResponseEncoder;
import org.joyqueue.broker.network.codec.BrokerPayloadCodecRegistrar;
import org.joyqueue.network.transport.TransportServer;
import org.joyqueue.network.transport.codec.Codec;
import org.joyqueue.network.transport.codec.CodecFactory;
import org.joyqueue.network.transport.codec.DefaultDecoder;
import org.joyqueue.network.transport.codec.DefaultEncoder;
import org.joyqueue.network.transport.codec.JoyQueueHeaderCodec;
import org.joyqueue.network.transport.codec.PayloadCodecFactory;
import org.joyqueue.network.transport.codec.support.DefaultCodec;
import org.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import org.joyqueue.network.transport.support.DefaultTransportServerFactory;
import org.joyqueue.store.StoreService;

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
