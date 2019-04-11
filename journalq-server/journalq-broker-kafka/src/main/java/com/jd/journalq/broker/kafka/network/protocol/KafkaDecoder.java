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
package com.jd.journalq.broker.kafka.network.protocol;

import com.jd.journalq.broker.kafka.command.KafkaRequestOrResponse;
import com.jd.journalq.broker.kafka.network.KafkaHeader;
import com.jd.journalq.network.transport.codec.DefaultDecoder;
import com.jd.journalq.network.transport.codec.PayloadCodecFactory;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * kafka解码
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class KafkaDecoder extends DefaultDecoder {
    private static Logger logger = LoggerFactory.getLogger(KafkaDecoder.class);

    public KafkaDecoder(KafkaHeaderCodec headerCodec, PayloadCodecFactory payloadCodecFactory) {
        super(headerCodec, payloadCodecFactory);
    }

    @Override
    public Object decode(ByteBuf buffer) throws TransportException.CodecException {
        Command command = (Command) super.decode(buffer);
        fillHeader((KafkaHeader) command.getHeader(), (KafkaRequestOrResponse) command.getPayload());

        // TODO 临时代码
        int readableBytes = buffer.readableBytes();
        if (readableBytes > 0 && readableBytes < 10) {
            logger.warn("skip bytes, command: {}, readableBytes: {}", command, readableBytes);
            buffer.skipBytes(readableBytes);
        }
        return command;
    }

    private void fillHeader(final KafkaHeader header, KafkaRequestOrResponse payload) throws TransportException.CodecException {
        payload.setVersion((short) header.getVersion());
        payload.setCorrelationId(header.getRequestId());
        payload.setClientId(header.getClientId());
        payload.setDirection(header.getDirection());
    }

    protected int readLength(ByteBuf buffer) {
        return buffer.readInt();
    }
}