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

import org.joyqueue.broker.kafka.config.KafkaConfig;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * GroupMetadataManager
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class GroupMetadataManager {

    protected static final Logger logger = LoggerFactory.getLogger(GroupMetadataManager.class);

    private KafkaConfig config;
    private org.joyqueue.broker.coordinator.group.GroupMetadataManager groupMetadataManager;

    public GroupMetadataManager(KafkaConfig config, org.joyqueue.broker.coordinator.group.GroupMetadataManager groupMetadataManager) {
        this.config = config;
        this.groupMetadataManager = groupMetadataManager;
    }

    public GroupMetadata getGroup(String groupId) {
        return groupMetadataManager.getGroup(groupId);
    }

    public GroupMetadata getOrCreateGroup(GroupMetadata group) {
        return groupMetadataManager.getOrCreateGroup(group);
    }

    public List<GroupMetadata> getGroups() {
        return groupMetadataManager.getGroups();
    }

    public boolean removeGroup(GroupMetadata group) {
        return groupMetadataManager.removeGroup(group.getId());
    }

    public boolean removeGroup(String groupId) {
        return groupMetadataManager.removeGroup(groupId);
    }
}