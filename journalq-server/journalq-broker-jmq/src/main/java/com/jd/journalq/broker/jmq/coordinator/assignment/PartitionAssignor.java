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
package com.jd.journalq.broker.jmq.coordinator.assignment;

import com.jd.journalq.broker.jmq.coordinator.domain.JMQCoordinatorGroup;
import com.jd.journalq.broker.jmq.coordinator.domain.JMQCoordinatorGroupMember;
import com.jd.journalq.broker.jmq.coordinator.domain.PartitionAssignment;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.laf.extension.Type;

import java.util.List;

/**
 * PartitionAssignor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public interface PartitionAssignor extends Type {

    PartitionAssignment assign(JMQCoordinatorGroup group, JMQCoordinatorGroupMember member, String topic, List<PartitionGroup> partitionGroups);
}