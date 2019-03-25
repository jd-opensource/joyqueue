package com.jd.journalq.broker.mqtt.network;

import com.jd.journalq.broker.mqtt.transport.MqttCommandInvocation;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.mqtt.handler.MqttHandlerDispatcher;
import com.jd.journalq.broker.network.protocol.support.DefaultProtocolHandlerPipeline;
import com.jd.journalq.network.handler.ConnectionHandler;
import com.jd.journalq.network.protocol.Protocol;
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
            e.printStackTrace();
        }
        return mqttHandlerDispatcher;
    }
}
