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
package com.jd.journalq.broker.kafka.handler;

import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.command.DescribeGroupsResponse;
import com.jd.journalq.broker.kafka.coordinator.GroupCoordinator;
import com.jd.journalq.broker.kafka.coordinator.domain.GroupDescribe;
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
