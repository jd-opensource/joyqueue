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
package org.joyqueue.nsr.support;

import org.joyqueue.domain.Replica;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.service.PartitionGroupReplicaService;
import org.joyqueue.nsr.service.internal.PartitionGroupReplicaInternalService;

import java.util.List;

/**
 * DefaultPartitionGroupReplicaService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class DefaultPartitionGroupReplicaService implements PartitionGroupReplicaService {

    private PartitionGroupReplicaInternalService partitionGroupReplicaInternalService;

    public DefaultPartitionGroupReplicaService(PartitionGroupReplicaInternalService partitionGroupReplicaInternalService) {
        this.partitionGroupReplicaInternalService = partitionGroupReplicaInternalService;
    }

    @Override
    public Replica getById(String id) {
        return partitionGroupReplicaInternalService.getById(id);
    }

    @Override
    public List<Replica> getByTopic(TopicName topic) {
        return partitionGroupReplicaInternalService.getByTopic(topic);
    }

    @Override
    public List<Replica> getByTopicAndGroup(TopicName topic, int groupNo) {
        return partitionGroupReplicaInternalService.getByTopicAndGroup(topic, groupNo);
    }

    @Override
    public List<Replica> getByBrokerId(Integer brokerId) {
        return partitionGroupReplicaInternalService.getByBrokerId(brokerId);
    }

    @Override
    public List<Replica> getAll() {
        return partitionGroupReplicaInternalService.getAll();
    }

    @Override
    public Replica add(Replica replica) {
        return partitionGroupReplicaInternalService.add(replica);
    }

    @Override
    public Replica update(Replica replica) {
        return partitionGroupReplicaInternalService.update(replica);
    }

    @Override
    public void delete(String id) {
        partitionGroupReplicaInternalService.delete(id);
    }
}
