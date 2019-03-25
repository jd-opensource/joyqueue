package com.jd.journalq.broker.jmq.handler.mqtt;

import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.common.domain.Subscription;
import com.jd.journalq.common.network.command.GetTopics;
import com.jd.journalq.common.network.command.GetTopicsAck;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.Direction;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.common.network.transport.exception.TransportException;
import com.jd.journalq.nsr.NameService;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2018/10/19
 */
public class GetTopicsHandler implements JMQCommandHandler, Type, BrokerContextAware {
    private NameService nameService;
    @Override
    public int type() {
        return JMQCommandType.MQTT_GET_TOPICS.getCode();
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.nameService = brokerContext.getNameService();
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        GetTopics getTopics = (GetTopics) command.getPayload();
        Set<String> topics = new HashSet<>();
        if(StringUtils.isBlank(getTopics.getApp())){
            topics.addAll(nameService.getAllTopics());
        }else {
            topics.addAll(nameService.getTopics(getTopics.getApp(), Subscription.Type.valueOf((byte)getTopics.getSubscribeType())));
        }
        return new Command(new JMQHeader(Direction.RESPONSE, JMQCommandType.MQTT_GET_TOPICS_ACK.getCode()),new GetTopicsAck().topics(topics));
    }
}
