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
package org.joyqueue.broker.protocol.coordinator.assignment;

import com.google.common.collect.Maps;
import org.joyqueue.broker.protocol.config.JoyQueueConfig;
import org.joyqueue.broker.protocol.coordinator.domain.GroupMemberMetadata;
import org.joyqueue.broker.protocol.coordinator.domain.GroupMetadata;
import org.joyqueue.broker.protocol.coordinator.domain.PartitionAssignment;
import org.joyqueue.broker.protocol.exception.JoyQueueException;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.exception.JoyQueueCode;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import com.jd.laf.extension.SpiLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * PartitionAssignor
 *
 * author: gaohaoxiang
 * date: 2018/12/5
 */
public class PartitionAssignorResolver {

    protected static final Logger logger = LoggerFactory.getLogger(PartitionAssignorResolver.class);

    private JoyQueueConfig config;
    private ExtensionPoint<PartitionAssignor, String> partitionAssignors = new ExtensionPointLazy<>(PartitionAssignor.class, SpiLoader.INSTANCE, null, null);

    public PartitionAssignorResolver(JoyQueueConfig config) {
        this.config = config;
    }

    public PartitionAssignment assign(GroupMetadata group, GroupMemberMetadata member, String topic, List<PartitionGroup> partitionGroups) {
        String assignType = config.getCoordinatorPartitionAssignType();
        PartitionAssignor partitionAssignor = (StringUtils.isBlank(assignType) ? null : partitionAssignors.get(assignType));

        if (partitionAssignor == null) {
            throw new JoyQueueException(JoyQueueCode.FW_COORDINATOR_PARTITION_ASSIGNOR_TYPE_NOT_EXIST.getMessage(assignType),
                    JoyQueueCode.FW_COORDINATOR_PARTITION_ASSIGNOR_TYPE_NOT_EXIST.getCode());
        }

        if (!StringUtils.equals(group.getAssignType(), assignType)) {
            group.setAssignContext(Maps.newHashMap());
            group.setAssignType(assignType);
            member.setTimeoutCallback(null);
        }

        return partitionAssignor.assign(group, member, topic, partitionGroups);
    }
}