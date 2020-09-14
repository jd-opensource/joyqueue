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
import org.joyqueue.domain.Replica;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.sql.domain.PartitionGroupReplicaDTO;

import java.util.Collections;
import java.util.List;

/**
 * PartitionGroupReplicaConverter
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class PartitionGroupReplicaConverter {

    public static String generateId(Replica replica) {
        return generateId(replica.getTopic().getFullName(), replica.getGroup(), replica.getBrokerId());
    }

    public static String generateId(String topic, int group, int replica) {
        return String.format("%s.%s.%s", topic, group, replica);
    }

    public static PartitionGroupReplicaDTO convert(Replica replica) {
        if (replica == null) {
            return null;
        }
        PartitionGroupReplicaDTO partitionGroupReplicaDTO = new PartitionGroupReplicaDTO();
        partitionGroupReplicaDTO.setId(generateId(replica));
        partitionGroupReplicaDTO.setTopic(replica.getTopic().getCode());
        partitionGroupReplicaDTO.setNamespace(replica.getTopic().getNamespace());
        partitionGroupReplicaDTO.setGroup(replica.getGroup());
        partitionGroupReplicaDTO.setBrokerId(Long.valueOf(replica.getBrokerId()));
        return partitionGroupReplicaDTO;
    }

    public static Replica convert(PartitionGroupReplicaDTO partitionGroupReplicaDTO) {
        if (partitionGroupReplicaDTO == null) {
            return null;
        }
        Replica replica = new Replica();
        replica.setId(partitionGroupReplicaDTO.getId());
        replica.setTopic(TopicName.parse(partitionGroupReplicaDTO.getTopic(), partitionGroupReplicaDTO.getNamespace()));
        replica.setBrokerId(Integer.valueOf(String.valueOf(partitionGroupReplicaDTO.getBrokerId())));
        replica.setGroup(partitionGroupReplicaDTO.getGroup());
        return replica;
    }

    public static List<Replica> convert(List<PartitionGroupReplicaDTO> partitionGroupReplicaDTOS) {
        if (CollectionUtils.isEmpty(partitionGroupReplicaDTOS)) {
            return Collections.emptyList();
        }
        List<Replica> result = Lists.newArrayListWithCapacity(partitionGroupReplicaDTOS.size());
        for (PartitionGroupReplicaDTO partitionGroupReplicaDTO : partitionGroupReplicaDTOS) {
            result.add(convert(partitionGroupReplicaDTO));
        }
        return result;
    }
}