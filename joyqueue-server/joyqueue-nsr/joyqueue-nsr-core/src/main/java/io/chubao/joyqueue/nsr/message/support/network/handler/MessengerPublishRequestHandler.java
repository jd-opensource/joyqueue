package io.chubao.joyqueue.nsr.message.support.network.handler;

import com.alibaba.fastjson.JSON;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandler;
import io.chubao.joyqueue.nsr.message.support.network.command.MessengerPublishRequest;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.chubao.joyqueue.toolkit.concurrent.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MessengerPublishRequestHandler
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerPublishRequestHandler implements CommandHandler, Type {

    protected static final Logger logger = LoggerFactory.getLogger(MessengerPublishRequestHandler.class);

    private EventBus eventBus;

    public MessengerPublishRequestHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public Command handle(Transport transport, Command command) {
        MessengerPublishRequest payload = (MessengerPublishRequest) command.getPayload();
        MetaEvent event = payload.getEvent();

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("onEvent, event: {}", JSON.toJSONString(event));
            }

            eventBus.inform(event);
            return BooleanAck.build();
        } catch (Exception e) {
            logger.error("messenger inform exception, event: {}", event, e);
            return BooleanAck.build(JoyQueueCode.CN_UNKNOWN_ERROR);
        }
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_MESSENGER_PUBLISH_REQUEST;
    }
}