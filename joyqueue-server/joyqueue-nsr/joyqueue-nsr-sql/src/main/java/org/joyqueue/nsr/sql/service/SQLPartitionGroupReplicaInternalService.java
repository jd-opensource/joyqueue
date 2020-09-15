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
package org.joyqueue.nsr.sql.service;

import org.joyqueue.domain.Replica;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.sql.converter.PartitionGroupReplicaConverter;
import org.joyqueue.nsr.sql.repository.PartitionGroupReplicaRepository;
import org.joyqueue.nsr.service.internal.PartitionGroupReplicaInternalService;

import java.util.List;

/**
 * SQLPartitionGroupReplicaInternalService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class SQLPartitionGroupReplicaInternalService implements PartitionGroupReplicaInternalService {

    private PartitionGroupReplicaRepository partitionGroupReplicaRepository;

    public SQLPartitionGroupReplicaInternalService(PartitionGroupReplicaRepository partitionGroupReplicaRepository) {
        this.partitionGroupReplicaRepository = partitionGroupReplicaRepository;
    }

    @Override
    public Replica getById(String id) {
        return PartitionGroupReplicaConverter.convert(partitionGroupReplicaRepository.getById(id));
    }

    @Override
    public List<Replica> getByTopic(TopicName topic) {
        return PartitionGroupReplicaConverter.convert(partitionGroupReplicaRepository.getByTopic(topic.getCode(), topic.getNamespace()));
    }

    @Override
    public List<Replica> getByTopicAndGroup(TopicName topic, int groupNo) {
        return PartitionGroupReplicaConverter.convert(partitionGroupReplicaRepository.getByTopicAndGroup(topic.getCode(), topic.getNamespace(), groupNo));
    }

    @Override
    public List<Replica> getByBrokerId(Integer brokerId) {
        return PartitionGroupReplicaConverter.convert(partitionGroupReplicaRepository.getByBrokerId(Long.valueOf(String.valueOf(brokerId))));
    }

    @Override
    public List<Replica> getAll() {
        return PartitionGroupReplicaConverter.convert(partitionGroupReplicaRepository.getAll());
    }

    @Override
    public Replica add(Replica replica) {
        return PartitionGroupReplicaConverter.convert(partitionGroupReplicaRepository.add(PartitionGroupReplicaConverter.convert(replica)));
    }

    @Override
    public Replica update(Replica replica) {
        return PartitionGroupReplicaConverter.convert(partitionGroupReplicaRepository.update(PartitionGroupReplicaConverter.convert(replica)));
    }

    @Override
    public void delete(String id) {
        partitionGroupReplicaRepository.deleteById(id);
    }
}