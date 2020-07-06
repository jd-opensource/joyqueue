/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.broker.election.handler;

import com.google.common.base.Preconditions;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.consumer.model.ConsumePartition;
import org.joyqueue.broker.consumer.position.model.Position;
import org.joyqueue.broker.election.ElectionConfig;
import org.joyqueue.broker.election.command.ReplicateConsumePosRequest;
import org.joyqueue.broker.election.command.ReplicateConsumePosResponse;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.command.handler.CommandHandler;
import org.joyqueue.network.transport.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/29
 */
public class ReplicateConsumePosRequestHandler implements CommandHandler, Type {
    private static Logger logger = LoggerFactory.getLogger(ReplicateConsumePosRequestHandler.class);

    private Consume consume;
    private ElectionConfig electionConfig;

    public ReplicateConsumePosRequestHandler(ElectionConfig electionConfig, Consume consume) {
        Preconditions.checkArgument(consume != null, "consume is null");

        this.electionConfig = electionConfig;
        this.consume = consume;
    }

    public ReplicateConsumePosRequestHandler(BrokerContext brokerContext) {
        Preconditions.checkArgument(brokerContext != null, "broker context is null");
        Preconditions.checkArgument(brokerContext.getConsume() != null, "consume is null");

        this.consume = brokerContext.getConsume();
        this.electionConfig = new ElectionConfig(brokerContext.getPropertySupplier());
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        if (!(command.getPayload() instanceof ReplicateConsumePosRequest)) {
            throw new IllegalArgumentException();
        }

        ReplicateConsumePosRequest request = (ReplicateConsumePosRequest)command.getPayload();
        JoyQueueHeader header = new JoyQueueHeader(Direction.RESPONSE, CommandType.REPLICATE_CONSUME_POS_RESPONSE);
        ReplicateConsumePosResponse response = new ReplicateConsumePosResponse(false);

        if (request.getConsumePositions() == null) {
            logger.info("Receive consume pos request, consume position is null");
            return new Command(header, response);
        }

        if (logger.isDebugEnabled() || electionConfig.getOutputConsumePos()) {
            logger.info("Receive consume pos request {}", request.getConsumePositions());
        }

        try {
            Map<ConsumePartition, Position> consumePositions = request.getConsumePositions();
            consume.setConsumePosition(consumePositions);
            response.setSuccess(true);
        } catch (Exception e) {
            logger.warn("Set consume info {} fail", request.getConsumePositions(), e);
            response.setSuccess(false);
        }

        return new Command(header, response);
    }

    @Override
    public int type() {
        return CommandType.REPLICATE_CONSUME_POS_REQUEST;
    }
}
