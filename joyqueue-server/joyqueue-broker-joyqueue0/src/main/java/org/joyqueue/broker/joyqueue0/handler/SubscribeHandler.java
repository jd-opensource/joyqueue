package org.joyqueue.broker.joyqueue0.handler;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandHandler;
import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Header;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.network.command.Subscribe;
import org.joyqueue.network.command.SubscribeAck;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.NameService;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/10/10
 */
@Deprecated
public class SubscribeHandler implements Joyqueue0CommandHandler, Type, BrokerContextAware {
    private NameService nameService;

    @Override
    public int type() {
        return Joyqueue0CommandType.SUBSCRIBE.getCode();
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.nameService = brokerContext.getNameService();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        Subscribe subscribe = (Subscribe) command.getPayload();
        List<TopicConfig> topicConfigs = nameService.subscribe(subscribe.getSubscriptions(), subscribe.getClientType());
        return new Command(new Joyqueue0Header(Direction.RESPONSE, Joyqueue0CommandType.SUBSCRIBE_ACK.getCode()), new SubscribeAck().topicConfigs(topicConfigs));
    }
}
