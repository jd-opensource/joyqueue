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
import org.joyqueue.broker.mqtt.network.MqttProtocolHandlerPipeline;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author majun8
 */
public class MqttProtocol implements ProtocolService, BrokerContextAware, ChannelHandlerProvider {
    private static final Logger logger = LoggerFactory.getLogger(MqttProtocol.class);

    private BrokerContext brokerContext;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
    }

    @Override
    public boolean isSupport(ByteBuf buffer) {
        return decodeProtocol(buffer);
    }

    private boolean decodeProtocol(ByteBuf buffer) {
        // from netty protocol implement

        // FixedHeader
        short b1 = buffer.readUnsignedByte();

        int messageType = b1 >> 4;
        boolean dupFlag = (b1 & 0x08) == 0x08;
        int qosLevel = (b1 & 0x06) >> 1;
        boolean retain = (b1 & 0x01) != 0;

        int remainingLength = 0;
        int multiplier = 1;
        short digit;
        int loops = 0;
        do {
            digit = buffer.readUnsignedByte();
            remainingLength += (digit & 127) * multiplier;
            multiplier *= 128;
            loops++;
        } while ((digit & 128) != 0 && loops < 4);

        // MQTT protocol limits Remaining Length to 4 bytes
        if (loops == 4 && (digit & 128) != 0) {
            return false;
        }

        if (buffer.readerIndex() >= buffer.writerIndex()) {
            return false;
        }

        // VariableHeader
        short msbSize = buffer.readUnsignedByte();
        short lsbSize = buffer.readUnsignedByte();
        int result = msbSize << 8 | lsbSize;
        String protocolName = buffer.toString(buffer.readerIndex(), result, CharsetUtil.UTF_8);
        if (!protocolName.equals("MQTT")) {
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
        return MqttConsts.PROTOCOL_MQTT_TYPE;
    }

    @Override
    public ChannelHandler getChannelHandler(ChannelHandler channelHandler) {
        return new MqttProtocolHandlerPipeline(this, channelHandler, brokerContext);
    }
}
