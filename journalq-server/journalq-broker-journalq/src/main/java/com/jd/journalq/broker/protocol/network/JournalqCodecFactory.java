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
package com.jd.journalq.broker.protocol.network;

import com.google.common.collect.Lists;
import com.jd.journalq.network.transport.codec.Codec;
import com.jd.journalq.network.transport.codec.CodecFactory;
import com.jd.journalq.network.transport.codec.support.JournalqCodec;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * JournalqCodecFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/28
 */
public class JournalqCodecFactory implements CodecFactory {

    private JournalqCodec codec;

    public JournalqCodecFactory() {
        this.codec = initCodec();
    }

    protected JournalqCodec initCodec() {
        JournalqCodec codec = new JournalqCodec();
        List<JournalqPayloadCodec> payloadCodecs = loadPayloadCodecs();
        for (JournalqPayloadCodec payloadCodec : payloadCodecs) {
            codec.getPayloadCodecFactory().register(payloadCodec);
        }
        return codec;
    }

    protected List<JournalqPayloadCodec> loadPayloadCodecs() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(JournalqPayloadCodec.class));
    }

    @Override
    public Codec getCodec() {
        return codec;
    }
}