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
package org.joyqueue.network.transport.codec.support;

import org.joyqueue.network.command.JoyQueuePayloadCodecRegistry;
import org.joyqueue.network.transport.codec.Codec;
import org.joyqueue.network.transport.codec.Decoder;
import org.joyqueue.network.transport.codec.DefaultDecoder;
import org.joyqueue.network.transport.codec.DefaultEncoder;
import org.joyqueue.network.transport.codec.Encoder;
import org.joyqueue.network.transport.codec.JoyQueueHeaderCodec;
import org.joyqueue.network.transport.codec.PayloadCodecFactory;
import org.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

/**
 * JoyQueueCodec
 *
 * author: gaohaoxiang
 * date: 2018/10/22
 */
public class JoyQueueCodec implements Codec {

    private Codec headerCodec;
    private PayloadCodecFactory payloadCodecFactory;

    private Decoder decoder;
    private Encoder encoder;

    public JoyQueueCodec() {
        PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
        JoyQueuePayloadCodecRegistry.register(payloadCodecFactory);
        this.headerCodec = new JoyQueueHeaderCodec();
        this.payloadCodecFactory = payloadCodecFactory;
        this.decoder = new DefaultDecoder(headerCodec, payloadCodecFactory);
        this.encoder = new DefaultEncoder(headerCodec, payloadCodecFactory);
    }

    public JoyQueueCodec(PayloadCodecFactory payloadCodecFactory) {
        this(new JoyQueueHeaderCodec(), payloadCodecFactory);
    }

    public JoyQueueCodec(Codec headerCodec, PayloadCodecFactory payloadCodecFactory) {
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
