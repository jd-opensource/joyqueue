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
package com.jd.joyqueue.broker.protocol.coordinator.assignment.support;

import com.jd.joyqueue.broker.protocol.coordinator.assignment.PartitionAssignor;
import com.jd.joyqueue.broker.protocol.coordinator.assignment.converter.PartitionAssignmentConverter;
import com.jd.joyqueue.broker.protocol.coordinator.domain.GroupMemberMetadata;
import com.jd.joyqueue.broker.protocol.coordinator.domain.GroupMetadata;
import com.jd.joyqueue.broker.protocol.coordinator.domain.PartitionAssignment;
import com.jd.joyqueue.domain.PartitionGroup;

import java.util.List;

/**
 * NonePartitionAssignor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class NonePartitionAssignor implements PartitionAssignor {

    @Override
    public PartitionAssignment assign(GroupMetadata group, GroupMemberMetadata member, String topic, List<PartitionGroup> partitionGroups) {
        return PartitionAssignmentConverter.convert(partitionGroups);
    }

    @Override
    public String type() {
        return "NONE";
    }
}