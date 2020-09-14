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
package org.joyqueue.nsr.sql.converter;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.sql.domain.PartitionGroupDTO;
import org.joyqueue.nsr.sql.helper.ArrayHelper;

import java.util.Collections;
import java.util.List;

/**
 * PartitionGroupConverter
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class PartitionGroupConverter {

    public static PartitionGroupDTO convert(PartitionGroup partitionGroup) {
        if (partitionGroup == null) {
            return null;
        }
        PartitionGroupDTO partitionGroupDTO = new PartitionGroupDTO();
        partitionGroupDTO.setId(generateId(partitionGroup));
        partitionGroupDTO.setTopic(partitionGroup.getTopic().getCode());
        partitionGroupDTO.setNamespace(partitionGroup.getTopic().getNamespace());
        partitionGroupDTO.setGroup(partitionGroup.getGroup());
        partitionGroupDTO.setPartitions(ArrayHelper.toString(partitionGroup.getPartitions()));
        partitionGroupDTO.setLeader(partitionGroup.getLeader());
        partitionGroupDTO.setRecLeader(partitionGroup.getRecLeader());
        partitionGroupDTO.setTerm(partitionGroup.getTerm());
        partitionGroupDTO.setReplicas(ArrayHelper.toString(partitionGroup.getReplicas()));
        partitionGroupDTO.setIsrs(ArrayHelper.toString(partitionGroup.getIsrs()));
        partitionGroupDTO.setLearners(ArrayHelper.toString(partitionGroup.getLearners()));
        partitionGroupDTO.setOutSyncReplicas(ArrayHelper.toString(partitionGroup.getOutSyncReplicas()));
        partitionGroupDTO.setElectType(Byte.valueOf((byte) partitionGroup.getElectType().type()));
        return partitionGroupDTO;
    }

    protected static String generateId(PartitionGroup partitionGroup) {
        return String.format("%s.%s", partitionGroup.getTopic().getFullName(), partitionGroup.getGroup());
    }

    public static PartitionGroup convert(PartitionGroupDTO partitionGroupDTO) {
        if (partitionGroupDTO == null) {
            return null;
        }
        PartitionGroup partitionGroup = new PartitionGroup();
        partitionGroup.setTopic(TopicName.parse(partitionGroupDTO.getTopic(), partitionGroupDTO.getNamespace()));
        partitionGroup.setGroup(partitionGroupDTO.getGroup());
        partitionGroup.setPartitions(ArrayHelper.toShortSet(partitionGroupDTO.getPartitions()));
        partitionGroup.setLeader(partitionGroupDTO.getLeader());
        partitionGroup.setRecLeader(partitionGroupDTO.getRecLeader());
        partitionGroup.setTerm(partitionGroupDTO.getTerm());
        partitionGroup.setReplicas(ArrayHelper.toIntSet(partitionGroupDTO.getReplicas()));
        partitionGroup.setIsrs(ArrayHelper.toIntSet(partitionGroupDTO.getIsrs()));
        partitionGroup.setLearners(ArrayHelper.toIntSet(partitionGroupDTO.getLearners()));
        partitionGroup.setOutSyncReplicas(ArrayHelper.toIntList(partitionGroupDTO.getOutSyncReplicas()));
        partitionGroup.setElectType(PartitionGroup.ElectType.valueOf(partitionGroupDTO.getElectType()));
        return partitionGroup;
    }

    public static List<PartitionGroup> convert(List<PartitionGroupDTO> partitionGroupDTOList) {
        if (CollectionUtils.isEmpty(partitionGroupDTOList)) {
            return Collections.emptyList();
        }
        List<PartitionGroup> result = Lists.newArrayListWithCapacity(partitionGroupDTOList.size());
        for (PartitionGroupDTO partitionGroupDTO : partitionGroupDTOList) {
            result.add(convert(partitionGroupDTO));
        }
        return result;
    }
}