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
package org.joyqueue.network.transport.codec;

import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.JoyQueuePayload;
import org.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultDecoder
 *
 * author: gaohaoxiang
 * date: 2018/8/21
 */
public class DefaultDecoder implements Decoder {

    // LENGTH
    private static final int LENGTH_FIELD_LENGTH = 4;

    protected static final Logger logger = LoggerFactory.getLogger(DefaultDecoder.class);

    private Codec headerCodec;
    private PayloadCodecFactory payloadCodecFactory;

    public DefaultDecoder(Codec headerCodec, PayloadCodecFactory payloadCodecFactory) {
        this.headerCodec = headerCodec;
        this.payloadCodecFactory = payloadCodecFactory;
    }

    @Override
    public Object decode(ByteBuf buffer) throws TransportException.CodecException {
        try {
            if (!buffer.isReadable(LENGTH_FIELD_LENGTH)) {
                logger.debug("Default decoder, readable bytes is {}", buffer.readableBytes());
                return null;
            }

            int readerIndex = buffer.readerIndex();
            int length = readLength(buffer);

            if (buffer.readableBytes() < length) {
                buffer.readerIndex(readerIndex);
                return null;
            }

            return doDecode(buffer);
        } catch (Exception e) {
            logger.error("decode exception", e);
            throw new TransportException.CodecException(e.getMessage());
        }
    }

    public Object doDecode(ByteBuf buffer) throws Exception {
        Header header = (Header) headerCodec.decode(buffer);
        if (header == null) {
            logger.info("Default doDecoder, header is null");
            return null;
        }

        PayloadDecoder decoder = payloadCodecFactory.getDecoder(header);
        if (decoder == null) {
            throw new TransportException.CodecException(String.format("unsupported decode payload type,header: %s", header));
        }

        Object payload = decoder.decode(header, buffer);

        if (payload instanceof JoyQueuePayload) {
            ((JoyQueuePayload) payload).setHeader(header);
        }

        return new Command(header, payload);

    }

    protected int readLength(ByteBuf buffer) {
        return buffer.readInt() - 4;
    }
}