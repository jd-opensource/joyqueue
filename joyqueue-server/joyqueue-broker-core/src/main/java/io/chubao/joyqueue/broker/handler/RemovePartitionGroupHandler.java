package io.chubao.joyqueue.broker.handler;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.election.ElectionService;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandler;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.chubao.joyqueue.nsr.network.command.RemovePartitionGroup;
import io.chubao.joyqueue.store.StoreService;
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
            return BooleanAck.build(JoyQueueCode.CN_UNKNOWN_ERROR, e.getMessage());
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
