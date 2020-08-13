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

import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.sql.converter.PartitionGroupConverter;
import org.joyqueue.nsr.sql.repository.PartitionGroupRepository;
import org.joyqueue.nsr.service.internal.PartitionGroupInternalService;

import java.util.List;

/**
 * SQLPartitionGroupInternalService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class SQLPartitionGroupInternalService implements PartitionGroupInternalService {

    private PartitionGroupRepository partitionGroupRepository;

    public SQLPartitionGroupInternalService(PartitionGroupRepository partitionGroupRepository) {
        this.partitionGroupRepository = partitionGroupRepository;
    }

    @Override
    public PartitionGroup getById(String id) {
        return PartitionGroupConverter.convert(partitionGroupRepository.getById(id));
    }

    @Override
    public PartitionGroup getByTopicAndGroup(TopicName topic, int group) {
        return PartitionGroupConverter.convert(partitionGroupRepository.getByTopicAndGroup(topic.getCode(), topic.getNamespace(), group));
    }

    @Override
    public List<PartitionGroup> getByTopic(TopicName topic) {
        return PartitionGroupConverter.convert(partitionGroupRepository.getByTopic(topic.getCode(), topic.getNamespace()));
    }

    @Override
    public List<PartitionGroup> getAll() {
        return PartitionGroupConverter.convert(partitionGroupRepository.getAll());
    }

    @Override
    public PartitionGroup add(PartitionGroup partitionGroup) {
        return PartitionGroupConverter.convert(partitionGroupRepository.add(PartitionGroupConverter.convert(partitionGroup)));
    }

    @Override
    public PartitionGroup update(PartitionGroup partitionGroup) {
        return PartitionGroupConverter.convert(partitionGroupRepository.update(PartitionGroupConverter.convert(partitionGroup)));
    }

    @Override
    public void delete(String id) {
        partitionGroupRepository.deleteById(id);
    }
}