package io.chubao.joyqueue.broker.kafka.handler;

import io.chubao.joyqueue.broker.kafka.KafkaContextAware;
import io.chubao.joyqueue.broker.kafka.coordinator.group.GroupCoordinator;
import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.KafkaContext;
import io.chubao.joyqueue.broker.kafka.command.LeaveGroupRequest;
import io.chubao.joyqueue.broker.kafka.command.LeaveGroupResponse;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LeaveGroupRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class LeaveGroupRequestHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(LeaveGroupRequestHandler.class);

    private GroupCoordinator groupCoordinator;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.groupCoordinator = kafkaContext.getGroupCoordinator();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        LeaveGroupRequest leaveGroupRequest = (LeaveGroupRequest) command.getPayload();
        short errorCode = groupCoordinator.handleLeaveGroup(leaveGroupRequest.getGroupId(), leaveGroupRequest.getMemberId());
        LeaveGroupResponse leaveGroupResponse = new LeaveGroupResponse(errorCode);
        return new Command(leaveGroupResponse);
    }

    @Override
    public int type() {
        return KafkaCommandType.LEAVE_GROUP.getCode();
    }
}
