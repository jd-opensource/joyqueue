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
package org.joyqueue.broker.protocol.coordinator;

import org.joyqueue.broker.protocol.config.JoyQueueConfig;
import org.joyqueue.broker.protocol.coordinator.domain.GroupMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GroupMetadataManager
 *
 * author: gaohaoxiang
 * date: 2018/12/5
 */
public class GroupMetadataManager {

    protected static final Logger logger = LoggerFactory.getLogger(GroupMetadataManager.class);

    private JoyQueueConfig config;
    private org.joyqueue.broker.coordinator.group.GroupMetadataManager groupMetadataManager;

    public GroupMetadataManager(JoyQueueConfig config, org.joyqueue.broker.coordinator.group.GroupMetadataManager groupMetadataManager) {
        this.config = config;
        this.groupMetadataManager = groupMetadataManager;
    }

    public GroupMetadata getGroup(String groupId) {
        return groupMetadataManager.getGroup(groupId);
    }

    public GroupMetadata getOrCreateGroup(GroupMetadata group) {
        return groupMetadataManager.getOrCreateGroup(group);
    }

    public boolean removeGroup(GroupMetadata group) {
        groupMetadataManager.removeGroup(group.getId());
        return true;
    }

    public boolean removeGroup(String groupId) {
        groupMetadataManager.removeGroup(groupId);
        return true;
    }
}