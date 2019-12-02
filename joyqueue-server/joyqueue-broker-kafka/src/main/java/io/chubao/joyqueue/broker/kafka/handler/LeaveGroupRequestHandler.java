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
package io.chubao.joyqueue.broker.kafka.handler;

import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.KafkaContext;
import io.chubao.joyqueue.broker.kafka.KafkaContextAware;
import io.chubao.joyqueue.broker.kafka.command.LeaveGroupRequest;
import io.chubao.joyqueue.broker.kafka.command.LeaveGroupResponse;
import io.chubao.joyqueue.broker.kafka.coordinator.group.GroupCoordinator;
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
        String groupId = leaveGroupRequest.getGroupId();
        short errorCode = groupCoordinator.handleLeaveGroup(groupId, leaveGroupRequest.getMemberId());
        LeaveGroupResponse leaveGroupResponse = new LeaveGroupResponse(errorCode);
        return new Command(leaveGroupResponse);
    }

    @Override
    public int type() {
        return KafkaCommandType.LEAVE_GROUP.getCode();
    }
}
