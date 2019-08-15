package io.chubao.joyqueue.broker.protocol.handler.mqtt;


import io.chubao.joyqueue.broker.protocol.JoyQueueCommandHandler;
import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.command.UnSubscribe;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.NameService;

/**
 * @author wylixiaobin
 * Date: 2018/10/10
 */
@Deprecated
public class UnSubscribeHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {
    private NameService nameService;

    @Override
    public int type() {
        return JoyQueueCommandType.MQTT_UNSUBSCRIBE.getCode();
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.nameService = brokerContext.getNameService();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        UnSubscribe unSubscribe = (UnSubscribe) command.getPayload();
        nameService.unSubscribe(unSubscribe.getSubscriptions());
        return BooleanAck.build();
    }
}
