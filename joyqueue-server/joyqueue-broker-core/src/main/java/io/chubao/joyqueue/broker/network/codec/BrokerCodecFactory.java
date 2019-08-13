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
package io.chubao.joyqueue.broker.network.codec;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.codec.CodecFactory;
import io.chubao.joyqueue.network.transport.codec.PayloadCodecFactory;
import io.chubao.joyqueue.network.transport.codec.support.JoyQueueCodec;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;


/**
 * BrokerCodecFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class BrokerCodecFactory implements CodecFactory {

    private static final Codec CODEC_INSTANCE = new BrokerCodecFactory().getCodec();

    private PayloadCodecFactory payloadCodecFactory;

    public BrokerCodecFactory() {
        this.payloadCodecFactory = initPayloadCodecFactory();
    }

    protected PayloadCodecFactory initPayloadCodecFactory() {
        PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
        List<BrokerPayloadCodec> brokerPayloadCodecs = loadPayloadCodecs();
        for (BrokerPayloadCodec brokerPayloadCodec : brokerPayloadCodecs) {
            payloadCodecFactory.register(brokerPayloadCodec);
        }

        // TODO 临时
        BrokerPayloadCodecRegistrar.register(payloadCodecFactory);
        return payloadCodecFactory;
    }

    protected List<BrokerPayloadCodec> loadPayloadCodecs() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(BrokerPayloadCodec.class));
    }

    @Override
    public Codec getCodec() {
        return new JoyQueueCodec(payloadCodecFactory);
    }

    public static Codec getInstance() {
        return CODEC_INSTANCE;
    }
}