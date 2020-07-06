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
package org.joyqueue.nsr.composition.service;

import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.composition.config.CompositionConfig;
import org.joyqueue.nsr.service.internal.PartitionGroupInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionPartitionGroupInternalService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionPartitionGroupInternalService implements PartitionGroupInternalService {

    protected final Logger logger = LoggerFactory.getLogger(CompositionPartitionGroupInternalService.class);

    private CompositionConfig config;
    private PartitionGroupInternalService ignitePartitionGroupService;
    private PartitionGroupInternalService journalkeeperPartitionGroupService;

    public CompositionPartitionGroupInternalService(CompositionConfig config, PartitionGroupInternalService ignitePartitionGroupService,
                                                    PartitionGroupInternalService journalkeeperPartitionGroupService) {
        this.config = config;
        this.ignitePartitionGroupService = ignitePartitionGroupService;
        this.journalkeeperPartitionGroupService = journalkeeperPartitionGroupService;
    }

    @Override
    public PartitionGroup getByTopicAndGroup(TopicName topic, int group) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupService.getByTopicAndGroup(topic, group);
        } else {
            try {
                return journalkeeperPartitionGroupService.getByTopicAndGroup(topic, group);
            } catch (Exception e) {
                logger.error("getByTopicAndGroup exception, topic: {}, group: {}", topic, group, e);
                return ignitePartitionGroupService.getByTopicAndGroup(topic, group);
            }
        }
    }

    @Override
    public List<PartitionGroup> getByTopic(TopicName topic) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupService.getByTopic(topic);
        } else {
            try {
                return journalkeeperPartitionGroupService.getByTopic(topic);
            } catch (Exception e) {
                logger.error("getByTopic exception, topic: {}", topic, e);
                return ignitePartitionGroupService.getByTopic(topic);
            }
        }
    }

    @Override
    public PartitionGroup getById(String id) {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupService.getById(id);
        } else {
            try {
                return journalkeeperPartitionGroupService.getById(id);
            } catch (Exception e) {
                logger.error("getById exception, id: {}", id, e);
                return ignitePartitionGroupService.getById(id);
            }
        }
    }

    @Override
    public List<PartitionGroup> getAll() {
        if (config.isReadIgnite()) {
            return ignitePartitionGroupService.getAll();
        } else {
            try {
                return journalkeeperPartitionGroupService.getAll();
            } catch (Exception e) {
                logger.error("getAll exception", e);
                return ignitePartitionGroupService.getAll();
            }
        }
    }

    @Override
    public PartitionGroup add(PartitionGroup partitionGroup) {
        PartitionGroup result = null;
        if (config.isWriteIgnite()) {
            result = ignitePartitionGroupService.add(partitionGroup);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperPartitionGroupService.add(partitionGroup);
            } catch (Exception e) {
                logger.error("add journalkeeper exception, params: {}", partitionGroup, e);
            }
        }
        return result;
    }

    @Override
    public PartitionGroup update(PartitionGroup partitionGroup) {
        PartitionGroup result = null;
        if (config.isWriteIgnite()) {
            result = ignitePartitionGroupService.update(partitionGroup);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperPartitionGroupService.update(partitionGroup);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", partitionGroup, e);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        if (config.isWriteIgnite()) {
            ignitePartitionGroupService.delete(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperPartitionGroupService.delete(id);
            } catch (Exception e) {
                logger.error("deleteById journalkeeper exception, params: {}", id, e);
            }
        }
    }
}
