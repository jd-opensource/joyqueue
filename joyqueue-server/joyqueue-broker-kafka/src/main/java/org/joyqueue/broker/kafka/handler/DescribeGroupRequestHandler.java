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
package org.joyqueue.broker.kafka.handler;

import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.KafkaContext;
import org.joyqueue.broker.kafka.KafkaContextAware;
import org.joyqueue.broker.kafka.command.DescribeGroupsRequest;
import org.joyqueue.broker.kafka.command.DescribeGroupsResponse;
import org.joyqueue.broker.kafka.coordinator.group.GroupCoordinator;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupDescribe;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;

import java.util.List;

/**
 * DescribeGroupRequestHandler
 *
 * author: gaohaoxiang
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
