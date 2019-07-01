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
package com.jd.joyqueue.server.retry.remote.handler;

import com.jd.joyqueue.exception.JoyQueueCode;
import com.jd.joyqueue.exception.JoyQueueException;
import com.jd.joyqueue.network.command.BooleanAck;
import com.jd.joyqueue.network.command.CommandType;
import com.jd.joyqueue.network.transport.Transport;
import com.jd.joyqueue.network.transport.codec.JoyQueueHeader;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.network.transport.command.Direction;
import com.jd.joyqueue.network.transport.command.handler.CommandHandler;
import com.jd.joyqueue.network.transport.exception.TransportException;
import com.jd.joyqueue.server.retry.api.MessageRetry;
import com.jd.joyqueue.server.retry.model.RetryMessageModel;
import com.jd.joyqueue.server.retry.remote.command.GetRetry;
import com.jd.joyqueue.server.retry.remote.command.GetRetryAck;
import com.jd.joyqueue.server.retry.remote.command.GetRetryCount;
import com.jd.joyqueue.server.retry.remote.command.GetRetryCountAck;
import com.jd.joyqueue.server.retry.remote.command.PutRetry;
import com.jd.joyqueue.server.retry.remote.command.UpdateRetry;
import com.jd.joyqueue.toolkit.time.SystemClock;
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
        logger.debug("Receive command:[{}]", command);

        long startTime = SystemClock.now();

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
                    throw new JoyQueueException(JoyQueueCode.CN_COMMAND_UNSUPPORTED.getMessage(command.getHeader().getType()),
                            JoyQueueCode.CN_COMMAND_UNSUPPORTED.getCode());
            }
        } catch (JoyQueueException e) {
            logger.error("Message retry exception, transport: {}", transport, e);
            return BooleanAck.build(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Message retry exception, transport: {}", transport, e);
            return BooleanAck.build(JoyQueueCode.CN_UNKNOWN_ERROR.getCode(), JoyQueueCode.CN_UNKNOWN_ERROR.getMessage());
        }finally {
            long endTime = SystemClock.now();

            if (endTime - startTime > 100) {
                logger.info("handle retry more than 100ms, Command:[{}]", command);
            }
        }
    }

    private Command execute(PutRetry putRetry) throws JoyQueueException {
        logger.debug("add retry message:[{}]", putRetry);

        List<RetryMessageModel> messages = putRetry.getMessages();
        messageRetry.addRetry(messages);

        return BooleanAck.build();
    }

    private Command execute(GetRetry getRetry) throws JoyQueueException {
        logger.debug("get retry message by condition:[{}]", getRetry);

        List<RetryMessageModel> retryMessageModelList = messageRetry.getRetry(getRetry.getTopic(), getRetry.getApp(), getRetry.getCount(), getRetry.getStartId());

        GetRetryAck payload = new GetRetryAck();
        payload.setMessages(retryMessageModelList);

        Command command = new Command();
        command.setHeader(new JoyQueueHeader(Direction.RESPONSE, CommandType.GET_RETRY_ACK));
        command.setPayload(payload);

        return command;
    }

    private Command execute(UpdateRetry updateRetry) throws JoyQueueException {
        logger.debug("update retry by condition:[{}]", updateRetry);

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

    private Command execute(GetRetryCount getRetryCount) throws JoyQueueException {
        logger.debug("get retry count by condition:[{}]", getRetryCount);

        int retryCount = messageRetry.countRetry(getRetryCount.getTopic(), getRetryCount.getApp());
        GetRetryCountAck getRetryCountAckPayload = new GetRetryCountAck();
        getRetryCountAckPayload.setTopic(getRetryCount.getTopic());
        getRetryCountAckPayload.setApp(getRetryCountAckPayload.getApp());
        getRetryCountAckPayload.setCount(retryCount);

        Command command = new Command();
        command.setHeader(new JoyQueueHeader(Direction.RESPONSE, CommandType.GET_RETRY_COUNT_ACK));
        command.setPayload(getRetryCountAckPayload);

        return command;
    }

}