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
package com.jd.journalq.broker.coordinator;

import com.jd.journalq.broker.coordinator.domain.JMQCoordinatorGroup;
import com.jd.journalq.broker.config.JournalqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMQCoordinatorGroupManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class JMQCoordinatorGroupManager {

    protected static final Logger logger = LoggerFactory.getLogger(JMQCoordinatorGroupManager.class);

    private JournalqConfig config;
    private CoordinatorGroupManager coordinatorGroupManager;

    public JMQCoordinatorGroupManager(JournalqConfig config, CoordinatorGroupManager coordinatorGroupManager) {
        this.config = config;
        this.coordinatorGroupManager = coordinatorGroupManager;
    }

    public JMQCoordinatorGroup getGroup(String groupId) {
        return coordinatorGroupManager.getGroup(groupId);
    }

    public JMQCoordinatorGroup getOrCreateGroup(JMQCoordinatorGroup group) {
        return coordinatorGroupManager.getOrCreateGroup(group);
    }

    public boolean removeGroup(JMQCoordinatorGroup group) {
        coordinatorGroupManager.removeGroup(group.getId());
        return true;
    }

    public boolean removeGroup(String groupId) {
        coordinatorGroupManager.removeGroup(groupId);
        return true;
    }
}