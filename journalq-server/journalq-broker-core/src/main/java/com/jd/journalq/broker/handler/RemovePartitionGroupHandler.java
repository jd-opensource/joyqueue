package com.jd.journalq.broker.handler;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.network.transport.command.handler.CommandHandler;
import com.jd.journalq.network.transport.exception.TransportException;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import com.jd.journalq.nsr.network.command.RemovePartitionGroup;
import com.jd.journalq.broker.election.ElectionService;
import com.jd.journalq.store.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wylixiaobin
 * Date: 2018/10/8
 */
public class RemovePartitionGroupHandler implements CommandHandler, Type {
    private static Logger logger = LoggerFactory.getLogger(RemovePartitionGroupHandler.class);
    private ElectionService electionService;
    private StoreService storeService;

    public RemovePartitionGroupHandler(BrokerContext brokerContext) {
        this.electionService = brokerContext.getElectionService();
        this.storeService = brokerContext.getStoreService();
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_REMOVE_PARTITIONGROUP;
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        if (command == null) {
            logger.error("CreatePartitionGroupHandler request command is null");
            return null;
        }
        RemovePartitionGroup request = ((RemovePartitionGroup) command.getPayload());
        PartitionGroup group = request.getPartitionGroup();
        try {
            if (logger.isDebugEnabled())
                logger.debug("begin removePartitionGroup topic[{}] partitionGroupRequest [{}] ", group.getTopic(), request);
            commit(group);
            return BooleanAck.build();
        } catch (Exception e) {
            logger.error(String.format("removePartitionGroup topic[{}] partitionGroupRequest [{}] error", group.getTopic(), request), e);
            return BooleanAck.build(JMQCode.CN_UNKNOWN_ERROR, e.getMessage());
        }
    }

    private void commit(PartitionGroup group) {
        if (logger.isDebugEnabled()) {
            logger.debug("topic[{}] remove partitionGroup[{}]", group.getTopic(), group.getGroup());
        }
        storeService.removePartitionGroup(group.getTopic().getFullName(), group.getGroup());
        electionService.onPartitionGroupRemove(group.getTopic(), group.getGroup());
    }

    private void rollback(Transport transport, Command command) {
        //do nothing
    }
}
