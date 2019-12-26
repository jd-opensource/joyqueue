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
package org.joyqueue.broker.mqtt.protocol;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.mqtt.MqttConsts;
import org.joyqueue.broker.mqtt.command.MqttHandlerFactory;
import org.joyqueue.network.transport.codec.CodecFactory;
import org.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import org.joyqueue.network.protocol.ChannelHandlerProvider;
import org.joyqueue.network.protocol.ProtocolService;
import org.joyqueue.broker.mqtt.network.MqttOverWebsocketProtocolHandlerPipeline;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.util.ByteProcessor;
import io.netty.util.internal.AppendableCharSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author majun8
 */
public class MqttOverWebsocketProtocol implements ProtocolService, BrokerContextAware, ChannelHandlerProvider {
    private static final Logger logger = LoggerFactory.getLogger(MqttOverWebsocketProtocol.class);

    private BrokerContext brokerContext;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
    }

    @Override
    public boolean isSupport(ByteBuf buffer) {
        // from netty protocol implement

        if (isNativeMqtt(buffer)) {
            return false;
        }

        if (!skipControlCharacters(buffer)) {
            return false;
        }
        int rIdx = buffer.readerIndex();

        LineParser lineParser = new LineParser(new AppendableCharSequence(65536), 4096);
        AppendableCharSequence line = lineParser.parse(buffer);
        String[] initialLine = splitInitialLine(line);
        if (initialLine.length < 3) {
            return false;
        }

        Map<String, String> headers = readHeaders(buffer);
        buffer.readerIndex(rIdx);
        if (!headers.containsKey("Connection") && !headers.get("Connection").equals("Upgrade")) {
            return false;
        }
        if (!headers.containsKey("Upgrade") && !headers.get("Upgrade").equals("websocket")) {
            return false;
        }
        if (!headers.containsKey("Sec-WebSocket-Protocol") && !headers.get("Sec-WebSocket-Protocol").equals("mqtt")) {
            return false;
        }

        return true;
    }

    @Override
    public CodecFactory createCodecFactory() {
        return null;
    }

    @Override
    public CommandHandlerFactory createCommandHandlerFactory() {
        MqttHandlerFactory mqttHandlerFactory = new MqttHandlerFactory();
        return MqttHandlerRegister.register(mqttHandlerFactory);
    }

    @Override
    public String type() {
        return MqttConsts.PROTOCOL_MQTT_OVER_WEBSOCKET_TYPE;
    }

    @Override
    public ChannelHandler getChannelHandler(ChannelHandler channelHandler) {
        return new MqttOverWebsocketProtocolHandlerPipeline(this, channelHandler, brokerContext);
    }

    private static boolean isNativeMqtt(ByteBuf byteBuf) {
        byteBuf.resetReaderIndex();
        if (byteBuf.readableBytes() < 2) {
            byteBuf.resetReaderIndex();
            return false;
        }
        byteBuf.resetReaderIndex();
        return true;
    }

    private static boolean skipControlCharacters(ByteBuf buffer) {
        boolean skiped = false;
        final int wIdx = buffer.writerIndex();
        int rIdx = buffer.readerIndex();
        while (wIdx > rIdx) {
            int c = buffer.getUnsignedByte(rIdx++);
            if (!Character.isISOControl(c) && !Character.isWhitespace(c)) {
                rIdx--;
                skiped = true;
                break;
            }
        }
        buffer.readerIndex(rIdx);
        return skiped;
    }

    private static String[] splitInitialLine(AppendableCharSequence sb) {
        int aStart;
        int aEnd;
        int bStart;
        int bEnd;
        int cStart;
        int cEnd;

        aStart = findNonWhitespace(sb, 0);
        aEnd = findWhitespace(sb, aStart);

        bStart = findNonWhitespace(sb, aEnd);
        bEnd = findWhitespace(sb, bStart);

        cStart = findNonWhitespace(sb, bEnd);
        cEnd = findEndOfString(sb);

        return new String[] {
                sb.subStringUnsafe(aStart, aEnd),
                sb.subStringUnsafe(bStart, bEnd),
                cStart < cEnd? sb.subStringUnsafe(cStart, cEnd) : "" };
    }

    private static int findNonWhitespace(AppendableCharSequence sb, int offset) {
        for (int result = offset; result < sb.length(); ++result) {
            if (!Character.isWhitespace(sb.charAtUnsafe(result))) {
                return result;
            }
        }
        return sb.length();
    }

    private static int findWhitespace(AppendableCharSequence sb, int offset) {
        for (int result = offset; result < sb.length(); ++result) {
            if (Character.isWhitespace(sb.charAtUnsafe(result))) {
                return result;
            }
        }
        return sb.length();
    }

    private static int findEndOfString(AppendableCharSequence sb) {
        for (int result = sb.length() - 1; result > 0; --result) {
            if (!Character.isWhitespace(sb.charAtUnsafe(result))) {
                return result + 1;
            }
        }
        return 0;
    }

    private Map<String, String> readHeaders(ByteBuf buffer) {
        Map<String, String> headers = new HashMap<>();

        CharSequence name = null;
        CharSequence value = null;
        HeaderParser headerParser = new HeaderParser(new AppendableCharSequence(65536), 8192);
        AppendableCharSequence line = headerParser.parse(buffer);
        if (line == null) {
            return headers;
        }
        if (line.length() > 0) {
            do {
                char firstChar = line.charAt(0);
                if (name != null && (firstChar == ' ' || firstChar == '\t')) {
                    String trimmedLine = line.toString().trim();
                    StringBuilder buf = new StringBuilder(value.length() + trimmedLine.length() + 1);
                    buf.append(value)
                            .append(' ')
                            .append(trimmedLine);
                    value = buf.toString();
                } else {
                    if (name != null) {
                        headers.put(name.toString(), value.toString());
                    }
                    final int length = line.length();
                    int nameStart;
                    int nameEnd;
                    int colonEnd;
                    int valueStart;
                    int valueEnd;

                    nameStart = findNonWhitespace(line, 0);
                    for (nameEnd = nameStart; nameEnd < length; nameEnd ++) {
                        char ch = line.charAt(nameEnd);
                        if (ch == ':' || Character.isWhitespace(ch)) {
                            break;
                        }
                    }

                    for (colonEnd = nameEnd; colonEnd < length; colonEnd ++) {
                        if (line.charAt(colonEnd) == ':') {
                            colonEnd ++;
                            break;
                        }
                    }

                    name = line.subStringUnsafe(nameStart, nameEnd);
                    valueStart = findNonWhitespace(line, colonEnd);
                    if (valueStart == length) {
                        value = "";
                    } else {
                        valueEnd = findEndOfString(line);
                        value = line.subStringUnsafe(valueStart, valueEnd);
                    }
                }

                line = headerParser.parse(buffer);
                if (line == null) {
                    return null;
                }
            } while (line.length() > 0);
        }

        // Add the last header.
        if (name != null) {
            headers.put(name.toString(), value.toString());
        }

        return headers;
    }

    private static class HeaderParser implements ByteProcessor {
        private final AppendableCharSequence seq;
        private final int maxLength;
        private int size;

        HeaderParser(AppendableCharSequence seq, int maxLength) {
            this.seq = seq;
            this.maxLength = maxLength;
        }

        public AppendableCharSequence parse(ByteBuf buffer) {
            final int oldSize = size;
            seq.reset();
            int i = buffer.forEachByte(this);
            if (i == -1) {
                size = oldSize;
                return null;
            }
            buffer.readerIndex(i + 1);
            return seq;
        }

        public void reset() {
            size = 0;
        }

        @Override
        public boolean process(byte value) throws Exception {
            char nextByte = (char) (value & 0xFF);
            if (nextByte == HttpConstants.CR) {
                return true;
            }
            if (nextByte == HttpConstants.LF) {
                return false;
            }

            if (++ size > maxLength) {
                // TODO: Respond with Bad Request and discard the traffic
                //    or close the connection.
                //       No need to notify the upstream handlers - just log.
                //       If decoding a response, just throw an exception.
                throw newException(maxLength);
            }

            seq.append(nextByte);
            return true;
        }

        protected TooLongFrameException newException(int maxLength) {
            return new TooLongFrameException("HTTP header is larger than " + maxLength + " bytes.");
        }
    }

    private static final class LineParser extends HeaderParser {

        LineParser(AppendableCharSequence seq, int maxLength) {
            super(seq, maxLength);
        }

        @Override
        public AppendableCharSequence parse(ByteBuf buffer) {
            reset();
            return super.parse(buffer);
        }

        @Override
        protected TooLongFrameException newException(int maxLength) {
            return new TooLongFrameException("An HTTP line is larger than " + maxLength + " bytes.");
        }
    }
}
