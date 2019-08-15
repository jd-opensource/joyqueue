package io.chubao.joyqueue.broker.kafka.handler;

import com.google.common.collect.Maps;
import io.chubao.joyqueue.broker.kafka.KafkaContextAware;
import io.chubao.joyqueue.broker.kafka.command.SyncGroupAssignment;
import io.chubao.joyqueue.broker.kafka.coordinator.group.GroupCoordinator;
import io.chubao.joyqueue.broker.kafka.coordinator.group.callback.SyncCallback;
import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.KafkaContext;
import io.chubao.joyqueue.broker.kafka.command.SyncGroupRequest;
import io.chubao.joyqueue.broker.kafka.command.SyncGroupResponse;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.exception.TransportException;
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