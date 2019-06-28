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
package com.jd.joyqueue.broker.mqtt.network;

import com.jd.joyqueue.broker.mqtt.transport.MqttCommandInvocation;
import com.jd.joyqueue.broker.BrokerContext;
import com.jd.joyqueue.broker.mqtt.handler.MqttHandlerDispatcher;
import com.jd.joyqueue.broker.network.protocol.support.DefaultProtocolHandlerPipeline;
import com.jd.joyqueue.network.handler.ConnectionHandler;
import com.jd.joyqueue.network.protocol.Protocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;

/**
 * @author majun8
 */
public class MqttProtocolHandlerPipeline extends ChannelInitializer {

    private Protocol protocol;
    private BrokerContext brokerContext;

    public MqttProtocolHandlerPipeline(Protocol protocol, ChannelHandler channelHandler, BrokerContext brokerContext) {
        this.protocol = protocol;
        this.brokerContext = brokerContext;
        if (channelHandler instanceof DefaultProtocolHandlerPipeline) {
            DefaultProtocolHandlerPipeline handlerPipeline = (DefaultProtocolHandlerPipeline) channelHandler;
            // todo
        }
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline()
                .addLast(new MqttDecoder())
                .addLast(MqttEncoder.INSTANCE)
                .addLast(new ConnectionHandler())
                .addLast(newMqttCommandInvocation());
    }

    protected MqttCommandInvocation newMqttCommandInvocation() {
        return new MqttCommandInvocation(newMqttHandlerDispatcher());
    }

    protected MqttHandlerDispatcher newMqttHandlerDispatcher() {
        MqttHandlerDispatcher mqttHandlerDispatcher = new MqttHandlerDispatcher(protocol.createCommandHandlerFactory(), brokerContext);
        try {
            mqttHandlerDispatcher.start();
        } catch (Exception e) {
        }
        return mqttHandlerDispatcher;
    }
}
