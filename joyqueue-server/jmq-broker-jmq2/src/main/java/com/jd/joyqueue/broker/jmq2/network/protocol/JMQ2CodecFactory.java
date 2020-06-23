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
package com.jd.joyqueue.broker.jmq2.network.protocol;

import com.google.common.collect.Lists;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import com.jd.joyqueue.broker.jmq2.network.codec.ResetConsumeOffsetCodec;
import com.jd.laf.extension.ExtensionManager;
import org.joyqueue.network.codec.AuthorizationCodec;
import org.joyqueue.network.codec.GetTopicsAckCodec;
import org.joyqueue.network.codec.GetTopicsCodec;
import org.joyqueue.network.codec.NullPayloadCodec;
import org.joyqueue.network.codec.SubscribeAckCodec;
import org.joyqueue.network.codec.SubscribeCodec;
import org.joyqueue.network.codec.UnSubscribeCodec;
import org.joyqueue.network.transport.codec.Codec;
import org.joyqueue.network.transport.codec.CodecFactory;
import org.joyqueue.network.transport.codec.DefaultDecoder;
import org.joyqueue.network.transport.codec.DefaultEncoder;
import org.joyqueue.network.transport.codec.PayloadCodecFactory;
import org.joyqueue.network.transport.codec.support.DefaultCodec;

import java.util.List;

/**
 * jmq编解码器工厂
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/27
 */
public class JMQ2CodecFactory implements CodecFactory {

    private PayloadCodecFactory payloadCodecFactory;

    public JMQ2CodecFactory() {
        this.payloadCodecFactory = initPayloadCodecFactory();
    }

    protected PayloadCodecFactory initPayloadCodecFactory() {
        List<JMQ2PayloadCodec> payloadCodecs = loadPayloadCodecs();
        PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
        for (JMQ2PayloadCodec payloadCodec : payloadCodecs) {
            payloadCodecFactory.register(payloadCodec);
        }

        // TODO 临时
        registerMqtt(payloadCodecFactory);
        return payloadCodecFactory;
    }

    protected void registerMqtt(PayloadCodecFactory payloadCodecFactory) {
        payloadCodecFactory.register(new SubscribeCodec());
        payloadCodecFactory.register(new SubscribeAckCodec());
        payloadCodecFactory.register(new UnSubscribeCodec());

        payloadCodecFactory.register(new NullPayloadCodec());

        payloadCodecFactory.register(new GetTopicsCodec());
        payloadCodecFactory.register(new GetTopicsAckCodec());
        payloadCodecFactory.register(new AuthorizationCodec());
        payloadCodecFactory.register(new ResetConsumeOffsetCodec());
    }

    protected List<JMQ2PayloadCodec> loadPayloadCodecs() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(JMQ2PayloadCodec.class));
    }

    @Override
    public Codec getCodec() {
        JMQ2HeaderCodec headerCodec = new JMQ2HeaderCodec();
        return new DefaultCodec(new DefaultDecoder(headerCodec, payloadCodecFactory), new DefaultEncoder(headerCodec, payloadCodecFactory));
    }
}