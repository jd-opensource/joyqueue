package com.jd.journalq.server.retry.remote.handler;

import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.exception.JMQException;
import com.jd.journalq.common.network.command.BooleanAck;
import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.Direction;
import com.jd.journalq.common.network.transport.command.handler.CommandHandler;
import com.jd.journalq.common.network.transport.exception.TransportException;
import com.jd.journalq.server.retry.api.MessageRetry;
import com.jd.journalq.server.retry.model.RetryMessageModel;
import com.jd.journalq.server.retry.remote.command.GetRetry;
import com.jd.journalq.server.retry.remote.command.GetRetryAck;
import com.jd.journalq.server.retry.remote.command.GetRetryCount;
import com.jd.journalq.server.retry.remote.command.GetRetryCountAck;
import com.jd.journalq.server.retry.remote.command.PutRetry;
import com.jd.journalq.server.retry.remote.command.UpdateRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 消息重试远程重试处理器
 * <p>
 * Created by chengzhiliang on 2018/9/25.
 */
public class RemoteRetryMessageHandler implements CommandHandler {

    protected static final Logger logger = LoggerFactory.getLogger(RemoteRetryMessageHandler.class);

    // 重试消息管理器
    private MessageRetry messageRetry;

    public RemoteRetryMessageHandler(MessageRetry messageRetry) {
        this.messageRetry = messageRetry;
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        try {
            switch (command.getHeader().getType()) {
                case CommandType.PUT_RETRY:
                    return execute((PutRetry) command.getPayload());
                case CommandType.GET_RETRY:
                    return execute((GetRetry) command.getPayload());
                case CommandType.UPDATE_RETRY:
                    return execute((UpdateRetry) command.getPayload());
                case CommandType.GET_RETRY_COUNT:
                    return execute((GetRetryCount) command.getPayload());
                default:
                    throw new JMQException(JMQCode.CN_COMMAND_UNSUPPORTED.getMessage(command.getHeader().getType()),
                            JMQCode.CN_COMMAND_UNSUPPORTED.getCode());
            }
        } catch (JMQException e) {
            logger.error("Message retry exception, transport: {}", transport, e);
            return BooleanAck.build(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Message retry exception, transport: {}", transport, e);
            return BooleanAck.build(JMQCode.CN_UNKNOWN_ERROR.getCode(), JMQCode.CN_UNKNOWN_ERROR.getMessage());
        }
    }

    private Command execute(PutRetry putRetry) throws JMQException {
        List<RetryMessageModel> messages = putRetry.getMessages();
        messageRetry.addRetry(messages);

        return BooleanAck.build();
    }

    private Command execute(GetRetry getRetry) throws JMQException {
        List<RetryMessageModel> retryMessageModelList = messageRetry.getRetry(getRetry.getTopic(), getRetry.getApp(), getRetry.getCount(), getRetry.getStartId());

        GetRetryAck payload = new GetRetryAck();
        payload.setMessages(retryMessageModelList);

        Command command = new Command();
        command.setHeader(new JMQHeader(Direction.RESPONSE, CommandType.GET_RETRY_ACK));
        command.setPayload(payload);

        return command;
    }

    private Command execute(UpdateRetry updateRetry) throws JMQException {
        int updateType = updateRetry.getUpdateType();
        if (UpdateRetry.SUCCESS == updateType) {
            messageRetry.retrySuccess(updateRetry.getTopic(), updateRetry.getApp(), updateRetry.getMessages());
        } else if (UpdateRetry.FAILED == updateType) {
            messageRetry.retryError(updateRetry.getTopic(), updateRetry.getApp(), updateRetry.getMessages());
        } else if (UpdateRetry.EXPIRED == updateType) {
            messageRetry.retryExpire(updateRetry.getTopic(), updateRetry.getApp(), updateRetry.getMessages());
        }

        return BooleanAck.build();
    }

    private Command execute(GetRetryCount getRetryCount) throws JMQException {
        int retryCount = messageRetry.countRetry(getRetryCount.getTopic(), getRetryCount.getApp());
        GetRetryCountAck getRetryCountAckPayload = new GetRetryCountAck();
        getRetryCountAckPayload.setTopic(getRetryCount.getTopic());
        getRetryCountAckPayload.setApp(getRetryCountAckPayload.getApp());
        getRetryCountAckPayload.setCount(retryCount);

        Command command = new Command();
        command.setHeader(new JMQHeader(Direction.RESPONSE, CommandType.GET_RETRY_COUNT_ACK));
        command.setPayload(getRetryCountAckPayload);

        return command;
    }

}