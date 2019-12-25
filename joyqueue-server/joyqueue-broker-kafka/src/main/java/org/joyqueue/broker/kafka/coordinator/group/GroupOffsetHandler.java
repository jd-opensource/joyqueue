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
package org.joyqueue.broker.kafka.coordinator.group;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.broker.kafka.KafkaErrorCode;
import org.joyqueue.broker.kafka.config.KafkaConfig;
import org.joyqueue.broker.kafka.coordinator.Coordinator;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupMetadata;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupState;
import org.joyqueue.broker.kafka.model.OffsetAndMetadata;
import org.joyqueue.broker.kafka.model.OffsetMetadataAndError;
import org.joyqueue.toolkit.service.Service;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * GroupOffsetHandler
 *
 * author: gaohaoxiang
 * date: 2018/11/7
 */
public class GroupOffsetHandler extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(GroupOffsetHandler.class);

    private KafkaConfig config;
    private Coordinator coordinator;
    private GroupMetadataManager groupMetadataManager;
    private GroupBalanceManager groupBalanceManager;
    private GroupOffsetManager groupOffsetManager;

    public GroupOffsetHandler(KafkaConfig config, Coordinator coordinator, GroupMetadataManager groupMetadataManager, GroupBalanceManager groupBalanceManager, GroupOffsetManager groupOffsetManager) {
        this.config = config;
        this.coordinator = coordinator;
        this.groupMetadataManager = groupMetadataManager;
        this.groupBalanceManager = groupBalanceManager;
        this.groupOffsetManager = groupOffsetManager;
    }

    public Map<String, List<OffsetMetadataAndError>> commitOffsets(String groupId, String memberId, int generationId, Map<String, List<OffsetAndMetadata>> offsets) {
        if (!isStarted()) {
            return buildCommitError(offsets, KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode());
        }

        if (!coordinator.isCurrentGroup(groupId)) {
            logger.info("group {} coordinator changed", groupId);
            return buildCommitError(offsets, KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode());
        }

        // 自主分配分区的情况
        if (StringUtils.isBlank(memberId)) {
            return groupOffsetManager.saveOffsets(groupId, offsets);
        }

        GroupMetadata group = groupMetadataManager.getGroup(groupId);
        if (group == null) {
            logger.info("offset commit, group({}) is null, member id is {}, generationId is {}, offsetMetadata is {}",
                    groupId, memberId, generationId, JSON.toJSONString(offsets));

            if (generationId < 0) {
                // the group is not relying on Kafka for partition management, so allow the commit
                return groupOffsetManager.saveOffsets(groupId, offsets);
            } else {
                // the group has failed over to this coordinator (which will be handled in KAFKA-2017),
                // or this is a request coming from an older generation. either way, reject the commit
                return buildCommitError(offsets, KafkaErrorCode.ILLEGAL_GENERATION.getCode());
            }
        }

        return handleCommitOffsets(group, groupId, memberId, generationId, offsets);
    }

    protected Map<String, List<OffsetMetadataAndError>> handleCommitOffsets(GroupMetadata group, String groupId, String memberId, int generationId, Map<String, List<OffsetAndMetadata>> offsets) {
        if (group.stateIs(GroupState.DEAD) || !group.isHasMember(memberId)) {
            return buildCommitError(offsets, KafkaErrorCode.UNKNOWN_MEMBER_ID.getCode());
        }
        if (group.stateIs(GroupState.EMPTY) && generationId < 0) {
            // The group is only using Kafka to store offsets.
            // Also, for transactional offset commits we don't need to validate group membership and the generation.
            return groupOffsetManager.saveOffsets(groupId, offsets);
        }
        if (group.stateIs(GroupState.AWAITINGSYNC)) {
            return buildCommitError(offsets, KafkaErrorCode.REBALANCE_IN_PROGRESS.getCode());
        }
        if (generationId != group.getGenerationId()) {
            return buildCommitError(offsets, KafkaErrorCode.ILLEGAL_GENERATION.getCode());
        }
        groupBalanceManager.completeAndScheduleNextHeartbeatExpiration(group, group.getMember(memberId));
        return groupOffsetManager.saveOffsets(groupId, offsets);
    }

    public Map<String, List<OffsetMetadataAndError>> fetchOffsets(String groupId, Map<String, List<Integer>> topicAndPartitions) {
        if (!isStarted()) {
            return buildFetchError(topicAndPartitions, KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode());
        }
        // return offsets blindly regardless the current group state since the group may be using
        // Kafka commit storage without automatic group management
        return groupOffsetManager.getOffsets(groupId, topicAndPartitions);
    }

    protected Map<String, List<OffsetMetadataAndError>> buildFetchError(Map<String, List<Integer>> topicAndPartitions, short errorCode) {
        Map<String, List<OffsetMetadataAndError>> result = Maps.newHashMapWithExpectedSize(topicAndPartitions.size());
        for (Map.Entry<String, List<Integer>> entry : topicAndPartitions.entrySet()) {
            List<OffsetMetadataAndError> offsetList = Lists.newArrayListWithCapacity(entry.getValue().size());
            result.put(entry.getKey(), offsetList);

            for (Integer partition : entry.getValue()) {
                offsetList.add(new OffsetMetadataAndError(partition, errorCode));
            }
        }
        return result;
    }

    protected Map<String, List<OffsetMetadataAndError>> buildCommitError(Map<String, List<OffsetAndMetadata>> offsets, short errorCode) {
        Map<String, List<OffsetMetadataAndError>> result = Maps.newHashMapWithExpectedSize(offsets.size());
        for (Map.Entry<String, List<OffsetAndMetadata>> entry : offsets.entrySet()) {
            List<OffsetMetadataAndError> offsetList = Lists.newArrayListWithCapacity(entry.getValue().size());
            result.put(entry.getKey(), offsetList);

            for (OffsetAndMetadata offsetAndMetadata : entry.getValue()) {
                offsetList.add(new OffsetMetadataAndError(offsetAndMetadata.getPartition(), errorCode));
            }
        }
        return result;
    }
}