package com.jd.journalq.broker.kafka.handler;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.coordinator.GroupCoordinator;
import com.jd.journalq.broker.kafka.coordinator.callback.JoinCallback;
import com.jd.journalq.broker.kafka.coordinator.domain.GroupJoinGroupResult;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.command.JoinGroupRequest;
import com.jd.journalq.broker.kafka.command.JoinGroupResponse;
import com.jd.journalq.broker.kafka.util.KafkaBufferUtils;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.exception.TransportException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JoinGroupHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class JoinGroupHandler extends AbstractKafkaCommandHandler implements KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(JoinGroupHandler.class);

    private GroupCoordinator groupCoordinator;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.groupCoordinator = kafkaContext.getGroupCoordinator();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        JoinGroupRequest joinGroupRequest = (JoinGroupRequest) command.getPayload();

        logger.info("join group, groupId = {}, clientId = {}, memberId = {}, ip = {}",
                joinGroupRequest.getGroupId(), joinGroupRequest.getClientId(),
                joinGroupRequest.getMemberId(), transport.remoteAddress().toString());

        JoinCallback callback = new JoinCallback() {
            @Override
            public void sendResponseCallback(GroupJoinGroupResult joinGroupResult) {
                handleJoinGroupResponse(transport, command, joinGroupRequest, joinGroupResult);
            }
        };

        groupCoordinator.handleJoinGroup(
                joinGroupRequest.getGroupId(),
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
