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
package org.joyqueue.broker.protocol.network;

import com.google.common.collect.Lists;
import org.joyqueue.broker.protocol.network.codec.JoyQueueCodec;
import org.joyqueue.broker.protocol.network.codec.JoyQueuePayloadCodec;
import org.joyqueue.network.transport.codec.Codec;
import org.joyqueue.network.transport.codec.CodecFactory;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * JoyQueueCodecFactory
 *
 * author: gaohaoxiang
 * date: 2019/2/28
 */
public class JoyQueueCodecFactory implements CodecFactory {

    private JoyQueueCodec codec;

    public JoyQueueCodecFactory() {
        this.codec = initCodec();
    }

    protected JoyQueueCodec initCodec() {
        JoyQueueCodec codec = new JoyQueueCodec();
        List<JoyQueuePayloadCodec> payloadCodecs = loadPayloadCodecs();
        for (JoyQueuePayloadCodec payloadCodec : payloadCodecs) {
            codec.getPayloadCodecFactory().register(payloadCodec);
        }
        return codec;
    }

    protected List<JoyQueuePayloadCodec> loadPayloadCodecs() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(JoyQueuePayloadCodec.class));
    }

    @Override
    public Codec getCodec() {
        return codec;
    }
}