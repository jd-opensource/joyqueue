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
package com.jd.journalq.network.transport.codec.support;

import com.jd.journalq.network.command.JournalqPayloadCodecRegistry;
import com.jd.journalq.network.transport.codec.Codec;
import com.jd.journalq.network.transport.codec.Decoder;
import com.jd.journalq.network.transport.codec.DefaultDecoder;
import com.jd.journalq.network.transport.codec.DefaultEncoder;
import com.jd.journalq.network.transport.codec.Encoder;
import com.jd.journalq.network.transport.codec.JMQHeaderCodec;
import com.jd.journalq.network.transport.codec.PayloadCodecFactory;
import com.jd.journalq.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

/**
 * JournalqCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/22
 */
public class JournalqCodec implements Codec {

    private Codec headerCodec;
    private PayloadCodecFactory payloadCodecFactory;

    private Decoder decoder;
    private Encoder encoder;

    public JournalqCodec() {
        PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
        JournalqPayloadCodecRegistry.register(payloadCodecFactory);
        this.headerCodec = new JMQHeaderCodec();
        this.payloadCodecFactory = payloadCodecFactory;
        this.decoder = new DefaultDecoder(headerCodec, payloadCodecFactory);
        this.encoder = new DefaultEncoder(headerCodec, payloadCodecFactory);
    }

    public JournalqCodec(PayloadCodecFactory payloadCodecFactory) {
        this(new JMQHeaderCodec(), payloadCodecFactory);
    }

    public JournalqCodec(Codec headerCodec, PayloadCodecFactory payloadCodecFactory) {
        this.headerCodec = headerCodec;
        this.payloadCodecFactory = payloadCodecFactory;
        this.decoder = new DefaultDecoder(headerCodec, payloadCodecFactory);
        this.encoder = new DefaultEncoder(headerCodec, payloadCodecFactory);
    }

    @Override
    public Object decode(ByteBuf buffer) throws TransportException.CodecException {
        return decoder.decode(buffer);
    }

    @Override
    public void encode(Object obj, ByteBuf buffer) throws TransportException.CodecException {
        encoder.encode(obj, buffer);
    }

    public PayloadCodecFactory getPayloadCodecFactory() {
        return payloadCodecFactory;
    }
}
