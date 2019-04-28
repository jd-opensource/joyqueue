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
package com.jd.journalq.broker.coordinator.assignment.support;

import com.jd.journalq.broker.coordinator.assignment.PartitionAssignor;
import com.jd.journalq.broker.coordinator.assignment.converter.PartitionAssignmentConverter;
import com.jd.journalq.broker.coordinator.domain.JMQCoordinatorGroup;
import com.jd.journalq.broker.coordinator.domain.JMQCoordinatorGroupMember;
import com.jd.journalq.broker.coordinator.domain.PartitionAssignment;
import com.jd.journalq.domain.PartitionGroup;

import java.util.List;

/**
 * NonePartitionAssignor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class NonePartitionAssignor implements PartitionAssignor {

    @Override
    public PartitionAssignment assign(JMQCoordinatorGroup group, JMQCoordinatorGroupMember member, String topic, List<PartitionGroup> partitionGroups) {
        return PartitionAssignmentConverter.convert(partitionGroups);
    }

    @Override
    public String type() {
        return "NONE";
    }
}