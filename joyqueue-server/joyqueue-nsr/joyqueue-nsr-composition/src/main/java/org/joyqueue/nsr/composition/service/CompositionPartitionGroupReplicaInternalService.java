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

import org.joyqueue.domain.Replica;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.composition.config.CompositionConfig;
import org.joyqueue.nsr.service.internal.PartitionGroupReplicaInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionPartitionGroupReplicaInternalService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionPartitionGroupReplicaInternalService implements PartitionGroupReplicaInternalService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionPartitionGroupReplicaInternalService.class);

    private CompositionConfig config;
    private PartitionGroupReplicaInternalService sourcePartitionGroupReplicaService;
    private PartitionGroupReplicaInternalService targetPartitionGroupReplicaService;

    public CompositionPartitionGroupReplicaInternalService(CompositionConfig config, PartitionGroupReplicaInternalService sourcePartitionGroupReplicaService,
                                                           PartitionGroupReplicaInternalService targetPartitionGroupReplicaService) {
        this.config = config;
        this.sourcePartitionGroupReplicaService = sourcePartitionGroupReplicaService;
        this.targetPartitionGroupReplicaService = targetPartitionGroupReplicaService;
    }

    @Override
    public List<Replica> getByTopic(TopicName topic) {
        if (config.isReadSource()) {
            return sourcePartitionGroupReplicaService.getByTopic(topic);
        } else {
            try {
                return targetPartitionGroupReplicaService.getByTopic(topic);
            } catch (Exception e) {
                logger.error("getByTopic exception, topic: {}", topic, e);
                return sourcePartitionGroupReplicaService.getByTopic(topic);
            }
        }
    }

    @Override
    public List<Replica> getByTopicAndGroup(TopicName topic, int groupNo) {
        if (config.isReadSource()) {
            return sourcePartitionGroupReplicaService.getByTopicAndGroup(topic, groupNo);
        } else {
            try {
                return targetPartitionGroupReplicaService.getByTopicAndGroup(topic, groupNo);
            } catch (Exception e) {
                logger.error("getByTopicAndGroup exception, topic: {}, groupNo: {}", topic, groupNo, e);
                return sourcePartitionGroupReplicaService.getByTopicAndGroup(topic, groupNo);
            }
        }
    }

    @Override
    public List<Replica> getByBrokerId(Integer brokerId) {
        if (config.isReadSource()) {
            return sourcePartitionGroupReplicaService.getByBrokerId(brokerId);
        } else {
            try {
                return targetPartitionGroupReplicaService.getByBrokerId(brokerId);
            } catch (Exception e) {
                logger.error("getByBrokerId exception, brokerId: {}", brokerId, e);
                return sourcePartitionGroupReplicaService.getByBrokerId(brokerId);
            }
        }
    }

    @Override
    public Replica getById(String id) {
        if (config.isReadSource()) {
            return sourcePartitionGroupReplicaService.getById(id);
        } else {
            try {
                return targetPartitionGroupReplicaService.getById(id);
            } catch (Exception e) {
                logger.error("getById exception", e);
                return sourcePartitionGroupReplicaService.getById(id);
            }
        }
    }

    @Override
    public List<Replica> getAll() {
        if (config.isReadSource()) {
            return sourcePartitionGroupReplicaService.getAll();
        } else {
            try {
                return targetPartitionGroupReplicaService.getAll();
            } catch (Exception e) {
                logger.error("getAll exception", e);
                return sourcePartitionGroupReplicaService.getAll();
            }
        }
    }

    @Override
    public Replica add(Replica replica) {
        Replica result = null;
        if (config.isWriteSource()) {
            result = sourcePartitionGroupReplicaService.add(replica);
        }
        if (config.isWriteTarget()) {
            try {
                targetPartitionGroupReplicaService.add(replica);
            } catch (Exception e) {
                logger.error("add exception, params: {}", replica, e);
            }
        }
        return result;
    }

    @Override
    public Replica update(Replica replica) {
        Replica result = null;
        if (config.isWriteSource()) {
            result = sourcePartitionGroupReplicaService.update(replica);
        }
        if (config.isWriteTarget()) {
            try {
                targetPartitionGroupReplicaService.update(replica);
            } catch (Exception e) {
                logger.error("update exception, params: {}", replica, e);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        if (config.isWriteSource()) {
            sourcePartitionGroupReplicaService.delete(id);
        }
        if (config.isWriteTarget()) {
            try {
                targetPartitionGroupReplicaService.delete(id);
            } catch (Exception e) {
                logger.error("delete exception, params: {}", id, e);
            }
        }
    }
}
