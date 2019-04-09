package com.jd.journalq.broker.election.handler;

import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.election.command.ReplicateConsumePosRequest;
import com.jd.journalq.broker.election.command.ReplicateConsumePosResponse;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.command.Direction;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.network.transport.command.handler.CommandHandler;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.exception.TransportException;
import com.jd.journalq.toolkit.lang.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/9/29
 */
public class ReplicateConsumePosRequestHandler implements CommandHandler, Type {
    private static Logger logger = LoggerFactory.getLogger(ReplicateConsumePosRequestHandler.class);

    private Consume consume;

    public ReplicateConsumePosRequestHandler(Consume consume) {
        Preconditions.checkArgument(consume != null, "consume is null");

        this.consume = consume;
    }

    public ReplicateConsumePosRequestHandler(BrokerContext brokerContext) {
        Preconditions.checkArgument(brokerContext != null, "broker context is null");
        Preconditions.checkArgument(brokerContext.getConsume() != null, "consume is null");

        this.consume = brokerContext.getConsume();
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        ReplicateConsumePosRequest request = (ReplicateConsumePosRequest)command.getPayload();
        boolean success;
        JMQHeader header = new JMQHeader(Direction.RESPONSE, CommandType.REPLICATE_CONSUME_POS_RESPONSE);
        ReplicateConsumePosResponse response = new ReplicateConsumePosResponse(false);

        if (request.getConsumePositions() == null) {
            logger.info("Receive consume pos request, consume position is null");
            return new Command(header, response);
        }

        logger.debug("Receive consume pos request {}", request.getConsumePositions());

        try {
            success = consume.setConsumeInfo(request.getConsumePositions());
            response.setSuccess(success);
        } catch (Exception e) {
            logger.warn("Set consume info {} fail", request.getConsumePositions(), e);
        }

        return new Command(header, response);
    }

    @Override
    public int type() {
        return CommandType.REPLICATE_CONSUME_POS_REQUEST;
    }
}
