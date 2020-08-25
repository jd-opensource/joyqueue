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
    private PartitionGroupInternalService sourcePartitionGroupService;
    private PartitionGroupInternalService targetPartitionGroupService;

    public CompositionPartitionGroupInternalService(CompositionConfig config, PartitionGroupInternalService sourcePartitionGroupService,
                                                    PartitionGroupInternalService targetPartitionGroupService) {
        this.config = config;
        this.sourcePartitionGroupService = sourcePartitionGroupService;
        this.targetPartitionGroupService = targetPartitionGroupService;
    }

    @Override
    public PartitionGroup getByTopicAndGroup(TopicName topic, int group) {
        if (config.isReadSource()) {
            return sourcePartitionGroupService.getByTopicAndGroup(topic, group);
        } else {
            try {
                return targetPartitionGroupService.getByTopicAndGroup(topic, group);
            } catch (Exception e) {
                logger.error("getByTopicAndGroup exception, topic: {}, group: {}", topic, group, e);
                return sourcePartitionGroupService.getByTopicAndGroup(topic, group);
            }
        }
    }

    @Override
    public List<PartitionGroup> getByTopic(TopicName topic) {
        if (config.isReadSource()) {
            return sourcePartitionGroupService.getByTopic(topic);
        } else {
            try {
                return targetPartitionGroupService.getByTopic(topic);
            } catch (Exception e) {
                logger.error("getByTopic exception, topic: {}", topic, e);
                return sourcePartitionGroupService.getByTopic(topic);
            }
        }
    }

    @Override
    public PartitionGroup getById(String id) {
        if (config.isReadSource()) {
            return sourcePartitionGroupService.getById(id);
        } else {
            try {
                return targetPartitionGroupService.getById(id);
            } catch (Exception e) {
                logger.error("getById exception, id: {}", id, e);
                return sourcePartitionGroupService.getById(id);
            }
        }
    }

    @Override
    public List<PartitionGroup> getAll() {
        if (config.isReadSource()) {
            return sourcePartitionGroupService.getAll();
        } else {
            try {
                return targetPartitionGroupService.getAll();
            } catch (Exception e) {
                logger.error("getAll exception", e);
                return sourcePartitionGroupService.getAll();
            }
        }
    }

    @Override
    public PartitionGroup add(PartitionGroup partitionGroup) {
        PartitionGroup result = null;
        if (config.isWriteSource()) {
            result = sourcePartitionGroupService.add(partitionGroup);
        }
        if (config.isWriteTarget()) {
            try {
                targetPartitionGroupService.add(partitionGroup);
            } catch (Exception e) {
                logger.error("add exception, params: {}", partitionGroup, e);
            }
        }
        return result;
    }

    @Override
    public PartitionGroup update(PartitionGroup partitionGroup) {
        PartitionGroup result = null;
        if (config.isWriteSource()) {
            result = sourcePartitionGroupService.update(partitionGroup);
        }
        if (config.isWriteTarget()) {
            try {
                targetPartitionGroupService.update(partitionGroup);
            } catch (Exception e) {
                logger.error("update exception, params: {}", partitionGroup, e);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        if (config.isWriteSource()) {
            sourcePartitionGroupService.delete(id);
        }
        if (config.isWriteTarget()) {
            try {
                targetPartitionGroupService.delete(id);
            } catch (Exception e) {
                logger.error("deleteById exception, params: {}", id, e);
            }
        }
    }
}
