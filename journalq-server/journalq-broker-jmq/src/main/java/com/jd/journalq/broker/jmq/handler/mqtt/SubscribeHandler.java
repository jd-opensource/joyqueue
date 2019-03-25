package com.jd.journalq.broker.jmq.handler.mqtt;

import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.Subscribe;
import com.jd.journalq.network.command.SubscribeAck;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Direction;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.NameService;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/10/10
 */
@Deprecated
public class SubscribeHandler implements JMQCommandHandler, Type, BrokerContextAware {
    private NameService nameService;

    @Override
    public int type() {
        return JMQCommandType.MQTT_SUBSCRIBE.getCode();
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.nameService = brokerContext.getNameService();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        Subscribe subscribe = (Subscribe) command.getPayload();
        List<TopicConfig> topicConfigs = nameService.subscribe(subscribe.getSubscriptions(), subscribe.getClientType());
        return new Command(new JMQHeader(Direction.RESPONSE, JMQCommandType.MQTT_SUBSCRIBE_ACK.getCode()), new SubscribeAck().topicConfigs(topicConfigs));
    }
}
