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

import com.google.common.collect.Maps;
import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.KafkaContext;
import org.joyqueue.broker.kafka.KafkaContextAware;
import org.joyqueue.broker.kafka.command.JoinGroupRequest;
import org.joyqueue.broker.kafka.command.JoinGroupResponse;
import org.joyqueue.broker.kafka.coordinator.group.GroupCoordinator;
import org.joyqueue.broker.kafka.coordinator.group.callback.JoinCallback;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupJoinGroupResult;
import org.joyqueue.broker.kafka.helper.KafkaClientHelper;
import org.joyqueue.broker.kafka.util.KafkaBufferUtils;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.exception.TransportException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JoinGroupRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class JoinGroupRequestHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(JoinGroupRequestHandler.class);

    private GroupCoordinator groupCoordinator;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.groupCoordinator = kafkaContext.getGroupCoordinator();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        JoinGroupRequest joinGroupRequest = (JoinGroupRequest) command.getPayload();
        String groupId = KafkaClientHelper.parseClient(joinGroupRequest.getClientId());

        logger.info("join group, groupId = {}, clientId = {}, memberId = {}, ip = {}",
                groupId, joinGroupRequest.getClientId(),
                joinGroupRequest.getMemberId(), transport.remoteAddress().toString());

        JoinCallback callback = new JoinCallback() {
            @Override
            public void sendResponseCallback(GroupJoinGroupResult joinGroupResult) {
                handleJoinGroupResponse(transport, command, joinGroupRequest, joinGroupResult);
            }
        };

        groupCoordinator.handleJoinGroup(
                groupId,
                joinGroupRequest.getMemberId(),
                joinGroupRequest.getClientId(),
                transport.remoteAddress().toString(),
                joinGroupRequest.getRebalanceTimeout(),
                joinGroupRequest.getSessionTimeout(),
                joinGroupRequest.getProtocolType(),
                buildProtocols(joinGroupRequest.getGroupProtocols()),
                callback);
        return null;
    }

    protected void handleJoinGroupResponse(Transport transport, Command request, JoinGroupRequest joinGroupRequest, GroupJoinGroupResult groupJoinGroupResult) {
        Map<String, byte[]> memberMap = groupJoinGroupResult.getMembers();
        Map<String, ByteBuffer> members = Maps.newHashMap();
        if (MapUtils.isNotEmpty(memberMap)) {
            for (Map.Entry<String, byte[]> entry : memberMap.entrySet()) {
                members.put(entry.getKey(), ByteBuffer.wrap(entry.getValue()));
            }
        }

        JoinGroupResponse joinGroupResponse = new JoinGroupResponse();
        joinGroupResponse.setErrorCode(groupJoinGroupResult.getErrorCode());
        joinGroupResponse.setGenerationId(groupJoinGroupResult.getGenerationId());
        joinGroupResponse.setGroupProtocol(groupJoinGroupResult.getSubProtocol());
        joinGroupResponse.setMemberId(groupJoinGroupResult.getMemberId());
        joinGroupResponse.setLeaderId(groupJoinGroupResult.getLeaderId());
        joinGroupResponse.setMembers(members);
        try {
            transport.acknowledge(request, new Command(joinGroupResponse));
        } catch (TransportException e) {
            logger.error("send join group response for {} failed: ", joinGroupRequest.getGroupId(), e);
        }
    }

    protected Map<String, byte[]> buildProtocols(List<JoinGroupRequest.ProtocolMetadata> protocolMetadatas) {
        if (CollectionUtils.isEmpty(protocolMetadatas)) {
            return Collections.emptyMap();
        }
        Map<String, byte[]> result = Maps.newHashMap();
        for (JoinGroupRequest.ProtocolMetadata protocolMetadata : protocolMetadatas) {
            String protocolName = protocolMetadata.name();
            byte[] protocolMata = KafkaBufferUtils.toArray(protocolMetadata.metadata());
            result.put(protocolName, protocolMata);
        }
        return result;
    }

    @Override
    public int type() {
        return KafkaCommandType.JOIN_GROUP.getCode();
    }
}
