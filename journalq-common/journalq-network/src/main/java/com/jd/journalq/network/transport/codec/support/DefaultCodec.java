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

import com.jd.journalq.network.transport.codec.Codec;
import com.jd.journalq.network.transport.codec.Decoder;
import com.jd.journalq.network.transport.codec.Encoder;
import com.jd.journalq.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

/**
 * 默认编解码器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public class DefaultCodec implements Codec {

    private Decoder decoder;
    private Encoder encoder;

    public DefaultCodec(Decoder decoder, Encoder encoder) {
        this.decoder = decoder;
        this.encoder = encoder;
    }

    @Override
    public Object decode(ByteBuf buffer) throws TransportException.CodecException {
        return decoder.decode(buffer);
    }

    @Override
    public void encode(Object obj, ByteBuf buffer) throws TransportException.CodecException {
        encoder.encode(obj, buffer);
    }
}