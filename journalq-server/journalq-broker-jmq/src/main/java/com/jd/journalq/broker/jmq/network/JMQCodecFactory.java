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
package com.jd.journalq.broker.jmq.network;

import com.google.common.collect.Lists;
import com.jd.journalq.network.transport.codec.Codec;
import com.jd.journalq.network.transport.codec.CodecFactory;
import com.jd.journalq.network.transport.codec.support.JMQCodec;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * JMQCodecFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/28
 */
public class JMQCodecFactory implements CodecFactory {

    private JMQCodec codec;

    public JMQCodecFactory() {
        this.codec = initCodec();
    }

    protected JMQCodec initCodec() {
        JMQCodec codec = new JMQCodec();
        List<JMQPayloadCodec> payloadCodecs = loadPayloadCodecs();
        for (JMQPayloadCodec payloadCodec : payloadCodecs) {
            codec.getPayloadCodecFactory().register(payloadCodec);
        }
        return codec;
    }

    protected List<JMQPayloadCodec> loadPayloadCodecs() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(JMQPayloadCodec.class));
    }

    @Override
    public Codec getCodec() {
        return codec;
    }
}