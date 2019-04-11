package com.jd.journalq.broker.kafka.handler;

import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.command.DescribeGroupsResponse;
import com.jd.journalq.broker.kafka.coordinator.group.GroupCoordinator;
import com.jd.journalq.broker.kafka.coordinator.group.domain.GroupDescribe;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.command.DescribeGroupsRequest;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;

import java.util.List;

/**
 * DescribeGroupHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class DescribeGroupHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    private GroupCoordinator groupCoordinator;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.groupCoordinator = kafkaContext.getGroupCoordinator();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        DescribeGroupsRequest request = (DescribeGroupsRequest) command.getPayload();
        List<String> groupIds = request.getGroupIds();

        List<GroupDescribe> groupDescribes = groupCoordinator.handleDescribeGroups(groupIds);
        DescribeGroupsResponse describeGroupsResponse = new DescribeGroupsResponse(groupDescribes);
        return new Command(describeGroupsResponse);
    }

    @Override
    public int type() {
        return KafkaCommandType.DESCRIBE_GROUP.getCode();
    }
}
