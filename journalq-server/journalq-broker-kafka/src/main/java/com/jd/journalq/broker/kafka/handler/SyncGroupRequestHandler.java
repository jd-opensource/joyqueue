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

import com.google.common.collect.Maps;
import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.command.SyncGroupAssignment;
import com.jd.journalq.broker.kafka.coordinator.group.GroupCoordinator;
import com.jd.journalq.broker.kafka.coordinator.group.callback.SyncCallback;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.command.SyncGroupRequest;
import com.jd.journalq.broker.kafka.command.SyncGroupResponse;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.exception.TransportException;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * SyncGroupRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/6
 */
public class SyncGroupRequestHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(SyncGroupRequestHandler.class);

    private GroupCoordinator groupCoordinator;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.groupCoordinator = kafkaContext.getGroupCoordinator();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        SyncGroupRequest syncGroupRequest = (SyncGroupRequest) command.getPayload();

        logger.info("sync group, groupId = {}, clientId = {}, memberId = {}",
                syncGroupRequest.getGroupId(), syncGroupRequest.getClientId(), syncGroupRequest.getMemberId());

        SyncCallback callback = new SyncCallback() {
            @Override
            public void sendResponseCallback(SyncGroupAssignment assignment, short errorCode) {
                handleSyncGroupResponse(transport, command, syncGroupRequest, assignment, errorCode);
            }
        };

        groupCoordinator.handleSyncGroup(
                syncGroupRequest.getGroupId(),
                syncGroupRequest.getGenerationId(),
                syncGroupRequest.getMemberId(),
                buildAssignmentMap(syncGroupRequest.getGroupAssignment()),
                callback);

        return null;
    }

    protected void handleSyncGroupResponse(Transport transport, Command request, SyncGroupRequest syncGroupRequest, SyncGroupAssignment assignment, short errorCode) {
        SyncGroupResponse syncGroupResponse = new SyncGroupResponse();
        syncGroupResponse.setAssignment(assignment);
        syncGroupResponse.setErrorCode(errorCode);
        try {
            transport.acknowledge(request, new Command(syncGroupResponse));
        } catch (TransportException e) {
            logger.error("send sync group response for {} failed: ", syncGroupRequest.getGroupId(), e);
        }
    }

    protected Map<String, SyncGroupAssignment> buildAssignmentMap(Map<String, SyncGroupAssignment> assignments) {
        if (MapUtils.isEmpty(assignments)) {
            return Collections.emptyMap();
        }
        Map<String, SyncGroupAssignment> result = Maps.newHashMap();
        for (Map.Entry<String, SyncGroupAssignment> entry : assignments.entrySet()) {
            String memberId = entry.getKey();
            result.put(memberId, entry.getValue());
        }
        return result;
    }

    @Override
    public int type() {
        return KafkaCommandType.SYNC_GROUP.getCode();
    }
}