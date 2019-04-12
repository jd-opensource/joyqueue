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
package com.jd.journalq.broker.kafka.coordinator;

import com.jd.journalq.broker.kafka.coordinator.domain.KafkaCoordinatorGroup;
import com.jd.journalq.broker.coordinator.CoordinatorGroupManager;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GroupMetadataManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class KafkaCoordinatorGroupManager {

    protected static final Logger logger = LoggerFactory.getLogger(KafkaCoordinatorGroupManager.class);

    private KafkaConfig config;
    private CoordinatorGroupManager coordinatorGroupManager;

    public KafkaCoordinatorGroupManager(KafkaConfig config, CoordinatorGroupManager coordinatorGroupManager) {
        this.config = config;
        this.coordinatorGroupManager = coordinatorGroupManager;
    }

    public KafkaCoordinatorGroup getGroup(String groupId) {
        return coordinatorGroupManager.getGroup(groupId);
    }

    public KafkaCoordinatorGroup getOrCreateGroup(KafkaCoordinatorGroup group) {
        return coordinatorGroupManager.getOrCreateGroup(group);
    }

    public boolean removeGroup(KafkaCoordinatorGroup group) {
        coordinatorGroupManager.removeGroup(group.getId());
        return true;
    }

    public boolean removeGroup(String groupId) {
        coordinatorGroupManager.removeGroup(groupId);
        return true;
    }
}