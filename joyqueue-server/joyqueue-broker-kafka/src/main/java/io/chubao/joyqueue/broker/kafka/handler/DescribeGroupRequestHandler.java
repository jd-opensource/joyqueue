package io.chubao.joyqueue.broker.kafka.handler;

import io.chubao.joyqueue.broker.kafka.KafkaContextAware;
import io.chubao.joyqueue.broker.kafka.command.DescribeGroupsResponse;
import io.chubao.joyqueue.broker.kafka.coordinator.group.GroupCoordinator;
import io.chubao.joyqueue.broker.kafka.coordinator.group.domain.GroupDescribe;
import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.KafkaContext;
import io.chubao.joyqueue.broker.kafka.command.DescribeGroupsRequest;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;

import java.util.List;

/**
 * DescribeGroupRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class DescribeGroupRequestHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

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
