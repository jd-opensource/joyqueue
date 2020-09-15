package org.joyqueue.broker.joyqueue0.handler;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandHandler;
import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Header;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.domain.Subscription;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.command.GetTopics;
import org.joyqueue.network.command.GetTopicsAck;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.nsr.NameService;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2018/10/19
 */
@Deprecated
public class GetTopicsHandler implements Joyqueue0CommandHandler, Type, BrokerContextAware {
    private NameService nameService;
    @Override
    public int type() {
        return CommandType.GET_TOPICS;
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
            topics.addAll(nameService.getAllTopicCodes());
        }else {
            topics.addAll(nameService.getTopics(getTopics.getApp(), Subscription.Type.valueOf((byte)getTopics.getSubscribeType())));
        }
        return new Command(new Joyqueue0Header(Direction.RESPONSE, Joyqueue0CommandType.GET_TOPICS_ACK.getCode()),new GetTopicsAck().topics(topics));
    }
}
