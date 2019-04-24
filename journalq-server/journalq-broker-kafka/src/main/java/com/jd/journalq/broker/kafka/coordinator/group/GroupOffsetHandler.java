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
package com.jd.journalq.broker.kafka.coordinator.group;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.Coordinator;
import com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMetadata;
import com.jd.journalq.broker.kafka.coordinator.group.domain.GroupState;
import com.jd.journalq.broker.kafka.model.OffsetAndMetadata;
import com.jd.journalq.broker.kafka.model.OffsetMetadataAndError;
import com.jd.journalq.toolkit.service.Service;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * GroupOffsetHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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

    public Table<String, Integer, OffsetMetadataAndError> commitOffsets(String groupId, String memberId, int generationId, Table<String, Integer, OffsetAndMetadata> offsetMetadata) {
        if (!isStarted()) {
            return buildCommitError(offsetMetadata, KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode());
        }

        if (!coordinator.isCurrentGroup(groupId)) {
            logger.info("group {} coordinator changed", groupId);
            groupMetadataManager.removeGroup(groupId);
            return buildCommitError(offsetMetadata, KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode());
        }

        // 自主分配分区的情况
        if (StringUtils.isBlank(memberId)) {
            return groupOffsetManager.saveOffsets(groupId, offsetMetadata);
        }

        GroupMetadata group = groupMetadataManager.getGroup(groupId);
        if (group == null) {
            logger.info("offset commit, group({}) is null, member id is {}, generationId is {}, offsetMetadata is {}",
                    groupId, memberId, generationId, JSON.toJSONString(offsetMetadata));

            if (generationId < 0) {
                // the group is not relying on Kafka for partition management, so allow the commit
                return groupOffsetManager.saveOffsets(groupId, offsetMetadata);
            } else {
                // the group has failed over to this coordinator (which will be handled in KAFKA-2017),
                // or this is a request coming from an older generation. either way, reject the commit
                return buildCommitError(offsetMetadata, KafkaErrorCode.ILLEGAL_GENERATION.getCode());
            }
        }

        synchronized (group) {
            return handleCommitOffsets(group, groupId, memberId, generationId, offsetMetadata);
        }
    }

    protected Table<String, Integer, OffsetMetadataAndError> handleCommitOffsets(GroupMetadata group, String groupId, String memberId, int generationId, Table<String, Integer, OffsetAndMetadata> offsetMetadata) {
        if (group.stateIs(GroupState.DEAD) || !group.isHasMember(memberId)) {
            return buildCommitError(offsetMetadata, KafkaErrorCode.UNKNOWN_MEMBER_ID.getCode());
        }
        if (group.stateIs(GroupState.EMPTY) && generationId < 0) {
            // The group is only using Kafka to store offsets.
            // Also, for transactional offset commits we don't need to validate group membership and the generation.
            return groupOffsetManager.saveOffsets(groupId, offsetMetadata);
        }
        if (group.stateIs(GroupState.AWAITINGSYNC)) {
            return buildCommitError(offsetMetadata, KafkaErrorCode.REBALANCE_IN_PROGRESS.getCode());
        }
        if (generationId != group.getGenerationId()) {
            return buildCommitError(offsetMetadata, KafkaErrorCode.ILLEGAL_GENERATION.getCode());
        }
        groupBalanceManager.completeAndScheduleNextHeartbeatExpiration(group, group.getMember(memberId));
        return groupOffsetManager.saveOffsets(groupId, offsetMetadata);
    }

    public Table<String, Integer, OffsetMetadataAndError> fetchOffsets(String groupId, HashMultimap<String, Integer> topicAndPartitions) {
        if (!isStarted()) {
            return buildFetchError(topicAndPartitions, KafkaErrorCode.COORDINATOR_NOT_AVAILABLE.getCode());
        }
        // return offsets blindly regardless the current group state since the group may be using
        // Kafka commit storage without automatic group management
        return groupOffsetManager.getOffsets(groupId, topicAndPartitions);
    }

    protected Table<String, Integer, OffsetMetadataAndError> buildFetchError(HashMultimap<String, Integer> topicAndPartitions, short errorCode) {
        Table<String, Integer, OffsetMetadataAndError> result = HashBasedTable.create();
        for (String topic : topicAndPartitions.keySet()) {
            for (Integer partition : topicAndPartitions.get(topic)) {
                result.put(topic, partition, new OffsetMetadataAndError(errorCode));
            }
        }
        return result;
    }

    protected Table<String, Integer, OffsetMetadataAndError> buildCommitError(Table<String, Integer, OffsetAndMetadata> offsetMetadata, short errorCode) {
        Table<String, Integer, OffsetMetadataAndError> result = HashBasedTable.create();
        for (String topic : offsetMetadata.rowKeySet()) {
            Map<Integer, OffsetAndMetadata> partitionsOffsetInfo = offsetMetadata.row(topic);
            for (Integer partition : partitionsOffsetInfo.keySet()) {
                result.put(topic, partition, new OffsetMetadataAndError(errorCode));
            }
        }
        return result;
    }
}