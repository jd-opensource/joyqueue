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
package org.joyqueue.broker.kafka.network.protocol;

import com.google.common.collect.Lists;
import org.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import org.joyqueue.network.transport.codec.Codec;
import org.joyqueue.network.transport.codec.CodecFactory;
import org.joyqueue.network.transport.codec.PayloadCodecFactory;
import org.joyqueue.network.transport.codec.support.DefaultCodec;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * KafkaCodecFactory
 *
 * author: gaohaoxiang
 * date: 2018/8/21
 */
public class KafkaCodecFactory implements CodecFactory {

    private PayloadCodecFactory payloadCodecFactory;

    public KafkaCodecFactory() {
        this.payloadCodecFactory = initPayloadCodecFactory();
    }

    protected PayloadCodecFactory initPayloadCodecFactory() {
        PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
        List<KafkaPayloadCodec> payloadCodecs = loadPayloadCodecs();
        for (KafkaPayloadCodec payloadCodec : payloadCodecs) {
            payloadCodecFactory.register(payloadCodec);
        }
        return payloadCodecFactory;
    }

    protected List<KafkaPayloadCodec> loadPayloadCodecs() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(KafkaPayloadCodec.class));
    }

    @Override
    public Codec getCodec() {
        KafkaHeaderCodec headerCodec = new KafkaHeaderCodec();
        return new DefaultCodec(new KafkaDecoder(headerCodec, payloadCodecFactory), new KafkaEncoder(headerCodec, payloadCodecFactory));
    }
}