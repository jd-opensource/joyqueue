package com.jd.journalq.broker.kafka.handler;

import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.coordinator.group.GroupCoordinator;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.command.LeaveGroupRequest;
import com.jd.journalq.broker.kafka.command.LeaveGroupResponse;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LeaveGroupHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class LeaveGroupHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(LeaveGroupHandler.class);

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
