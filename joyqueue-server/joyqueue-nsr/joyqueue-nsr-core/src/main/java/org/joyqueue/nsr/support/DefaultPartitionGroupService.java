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

import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.service.PartitionGroupService;
import org.joyqueue.nsr.service.internal.PartitionGroupInternalService;

import java.util.List;

/**
 * DefaultPartitionGroupService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class DefaultPartitionGroupService implements PartitionGroupService {

    private PartitionGroupInternalService partitionGroupInternalService;

    public DefaultPartitionGroupService(PartitionGroupInternalService partitionGroupInternalService) {
        this.partitionGroupInternalService = partitionGroupInternalService;
    }

    @Override
    public PartitionGroup getById(String id) {
        return partitionGroupInternalService.getById(id);
    }

    @Override
    public PartitionGroup getByTopicAndGroup(TopicName topic, int group) {
        return partitionGroupInternalService.getByTopicAndGroup(topic, group);
    }

    @Override
    public List<PartitionGroup> getByTopic(TopicName topic) {
        return partitionGroupInternalService.getByTopic(topic);
    }

    @Override
    public List<PartitionGroup> getAll() {
        return partitionGroupInternalService.getAll();
    }

    @Override
    public PartitionGroup add(PartitionGroup partitionGroup) {
        return partitionGroupInternalService.add(partitionGroup);
    }

    @Override
    public PartitionGroup update(PartitionGroup partitionGroup) {
        return partitionGroupInternalService.update(partitionGroup);
    }

    @Override
    public void delete(String id) {
        partitionGroupInternalService.delete(id);
    }
}
