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

import com.google.common.collect.Lists;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Topic;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.nsr.sql.converter.PartitionGroupConverter;
import org.joyqueue.nsr.sql.converter.PartitionGroupReplicaConverter;
import org.joyqueue.nsr.sql.converter.TopicConverter;
import org.joyqueue.nsr.sql.domain.PartitionGroupDTO;
import org.joyqueue.nsr.sql.domain.PartitionGroupReplicaDTO;
import org.joyqueue.nsr.sql.domain.TopicDTO;
import org.joyqueue.nsr.sql.repository.PartitionGroupReplicaRepository;
import org.joyqueue.nsr.sql.repository.PartitionGroupRepository;
import org.joyqueue.nsr.sql.repository.TopicRepository;
import org.joyqueue.nsr.model.TopicQuery;
import org.joyqueue.nsr.service.internal.TopicInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * SQLTopicInternalService
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class SQLTopicInternalService implements TopicInternalService {

    protected static final Logger logger = LoggerFactory.getLogger(SQLTopicInternalService.class);

    private TopicRepository topicRepository;
    private PartitionGroupRepository partitionGroupRepository;
    private PartitionGroupReplicaRepository partitionGroupReplicaRepository;

    public SQLTopicInternalService(TopicRepository topicRepository, PartitionGroupRepository partitionGroupRepository, PartitionGroupReplicaRepository partitionGroupReplicaRepository) {
        this.topicRepository = topicRepository;
        this.partitionGroupRepository = partitionGroupRepository;
        this.partitionGroupReplicaRepository = partitionGroupReplicaRepository;
    }

    @Override
    public Topic getById(String id) {
        return TopicConverter.convert(topicRepository.getById(id));
    }

    @Override
    public Topic getTopicByCode(String namespace, String topic) {
        return TopicConverter.convert(topicRepository.getByCodeAndNamespace(topic, namespace));
    }

    @Override
    public PageResult<Topic> search(QPageQuery<TopicQuery> pageQuery) {
        int count = topicRepository.getSearchCount(pageQuery.getQuery());
        List<TopicDTO> topics = null;
        if (count != 0) {
            topics = topicRepository.search(pageQuery);
        }

        Pagination pagination = pageQuery.getPagination();
        pagination.setTotalRecord(count);

        PageResult<Topic> result = new PageResult();
        result.setPagination(pagination);
        result.setResult(TopicConverter.convert(topics));
        return result;
    }

    @Override
    public PageResult<Topic> findUnsubscribedByQuery(QPageQuery<TopicQuery> pageQuery) {
        return search(pageQuery);
    }

    @Override
    public List<Topic> getAll() {
        return TopicConverter.convert(topicRepository.getAll());
    }

    @Override
    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {
        topicRepository.add(TopicConverter.convert(topic));

        for (PartitionGroup partitionGroup : partitionGroups) {
            partitionGroupRepository.add(PartitionGroupConverter.convert(partitionGroup));

            for (Integer replica : partitionGroup.getReplicas()) {
                partitionGroupReplicaRepository.add(new PartitionGroupReplicaDTO(
                        PartitionGroupReplicaConverter.generateId(topic.getName().getFullName(), partitionGroup.getGroup(), replica),
                        topic.getName().getCode(),
                        topic.getName().getNamespace(),
                        Long.valueOf(String.valueOf(replica)), partitionGroup.getGroup()));
            }
        }
    }

    @Override
    public void removeTopic(Topic topic) {
        TopicDTO topicDTO = topicRepository.getByCodeAndNamespace(topic.getName().getCode(), topic.getName().getNamespace());
        List<PartitionGroupDTO> partitionGroups = partitionGroupRepository.getByTopic(topicDTO.getCode(), topicDTO.getNamespace());
        List<PartitionGroupReplicaDTO> partitionGroupReplicas = partitionGroupReplicaRepository.getByTopic(topicDTO.getCode(), topicDTO.getNamespace());

        for (PartitionGroupDTO partitionGroup : partitionGroups) {
            partitionGroupRepository.deleteById(partitionGroup.getId());
        }

        for (PartitionGroupReplicaDTO partitionGroupReplica : partitionGroupReplicas) {
            partitionGroupReplicaRepository.deleteById(partitionGroupReplica.getId());
        }

        topicRepository.deleteById(topicDTO.getId());
    }

    @Override
    public void addPartitionGroup(PartitionGroup group) {
        TopicDTO topicDTO = topicRepository.getByCodeAndNamespace(group.getTopic().getCode(), group.getTopic().getNamespace());
        partitionGroupRepository.add(PartitionGroupConverter.convert(group));

        for (Integer replica : group.getReplicas()) {
            partitionGroupReplicaRepository.add(new PartitionGroupReplicaDTO(
                    PartitionGroupReplicaConverter.generateId(group.getTopic().getFullName(), group.getGroup(), replica),
                    group.getTopic().getCode(),
                    group.getTopic().getNamespace(),
                    Long.valueOf(String.valueOf(replica)), group.getGroup()));
        }

        topicRepository.incrPartitions(topicDTO.getId(), group.getPartitions().size());
    }

    @Override
    public void removePartitionGroup(PartitionGroup group) {
        TopicDTO topicDTO = topicRepository.getByCodeAndNamespace(group.getTopic().getCode(), group.getTopic().getNamespace());
        PartitionGroupDTO partitionGroup = partitionGroupRepository.getByTopicAndGroup(group.getTopic().getCode(), group.getTopic().getNamespace(), group.getGroup());
        List<PartitionGroupReplicaDTO> partitionGroupReplicas = partitionGroupReplicaRepository.getByTopicAndGroup(topicDTO.getCode(), topicDTO.getNamespace(), group.getGroup());
        partitionGroupRepository.deleteById(partitionGroup.getId());

        for (PartitionGroupReplicaDTO partitionGroupReplica : partitionGroupReplicas) {
            partitionGroupReplicaRepository.deleteById(partitionGroupReplica.getId());
        }

        topicRepository.decrPartitions(topicDTO.getId(), group.getPartitions().size());
    }

    @Override
    public Collection<Integer> updatePartitionGroup(PartitionGroup group) {
        TopicDTO topicDTO = topicRepository.getByCodeAndNamespace(group.getTopic().getCode(), group.getTopic().getNamespace());
        PartitionGroupDTO oldPartitionGroupDTO = partitionGroupRepository.getByTopicAndGroup(group.getTopic().getCode(), group.getTopic().getNamespace(), group.getGroup());
        PartitionGroup oldPartitionGroup = PartitionGroupConverter.convert(oldPartitionGroupDTO);
        List<PartitionGroupReplicaDTO> partitionGroupReplicas = partitionGroupReplicaRepository.getByTopicAndGroup(group.getTopic().getCode(), group.getTopic().getNamespace(), group.getGroup());

        for (PartitionGroupReplicaDTO partitionGroupReplica : partitionGroupReplicas) {
            if (!group.getReplicas().contains(partitionGroupReplica.getBrokerId())) {
                partitionGroupReplicaRepository.deleteById(partitionGroupReplica.getId());
            }
        }

        for (Integer replica : group.getReplicas()) {
            boolean isMatch = false;
            for (PartitionGroupReplicaDTO partitionGroupReplica : partitionGroupReplicas) {
                if (partitionGroupReplica.getBrokerId().equals(replica)) {
                    isMatch = true;
                    break;
                }
            }

            if (!isMatch) {
                partitionGroupReplicaRepository.add(new PartitionGroupReplicaDTO(
                        PartitionGroupReplicaConverter.generateId(group.getTopic().getFullName(), group.getGroup(), replica),
                        group.getTopic().getCode(),
                        group.getTopic().getNamespace(),
                        Long.valueOf(String.valueOf(replica)), group.getGroup()));
            }
        }

        partitionGroupRepository.update(PartitionGroupConverter.convert(group));

        if (group.getPartitions().size() != oldPartitionGroup.getPartitions().size()) {
            topicRepository.incrPartitions(topicDTO.getId(), group.getPartitions().size() - oldPartitionGroup.getPartitions().size());
        }

        return Collections.emptyList();
    }

    @Override
    public void leaderReport(PartitionGroup group) {
        partitionGroupRepository.updateLeader(PartitionGroupConverter.convert(group));
    }

    @Override
    public void leaderChange(PartitionGroup group) {

    }

    @Override
    public List<PartitionGroup> getPartitionGroup(String namespace, String topic, Object[] groups) {
        List<PartitionGroup> result = Lists.newLinkedList();
        for (Object group : groups) {
            PartitionGroupDTO partitionGroupDTO = partitionGroupRepository.getByTopicAndGroup(topic, namespace, Integer.valueOf(String.valueOf(group)));
            if (partitionGroupDTO != null) {
                result.add(PartitionGroupConverter.convert(partitionGroupDTO));
            }
        }
        return result;
    }

    @Override
    public Topic add(Topic topic) {
        return TopicConverter.convert(topicRepository.add(TopicConverter.convert(topic)));
    }

    @Override
    public Topic update(Topic topic) {
        return TopicConverter.convert(topicRepository.update(TopicConverter.convert(topic)));
    }
}