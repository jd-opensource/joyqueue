/**
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
package com.jd.joyqueue.broker.election;

import com.jd.joyqueue.broker.consumer.Consume;
import com.jd.joyqueue.broker.election.network.codec.*;
import com.jd.joyqueue.broker.network.codec.BrokerPayloadCodecRegistrar;
import com.jd.joyqueue.network.transport.TransportServer;
import com.jd.joyqueue.network.transport.codec.*;
import com.jd.joyqueue.network.transport.codec.support.DefaultCodec;
import com.jd.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import com.jd.joyqueue.network.transport.support.DefaultTransportServerFactory;
import com.jd.joyqueue.store.StoreService;

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
                JournalqHeaderCodec headerCodec = new JournalqHeaderCodec();
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
