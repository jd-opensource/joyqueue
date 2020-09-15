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
import org.joyqueue.domain.Topic;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.nsr.composition.config.CompositionConfig;
import org.joyqueue.nsr.model.TopicQuery;
import org.joyqueue.nsr.service.internal.TopicInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * CompositionTopicInternalService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionTopicInternalService implements TopicInternalService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionTopicInternalService.class);

    private CompositionConfig config;
    private TopicInternalService sourceTopicService;
    private TopicInternalService targetTopicService;

    public CompositionTopicInternalService(CompositionConfig config, TopicInternalService sourceTopicService, TopicInternalService targetTopicService) {
        this.config = config;
        this.sourceTopicService = sourceTopicService;
        this.targetTopicService = targetTopicService;
    }

    @Override
    public Topic getTopicByCode(String namespace, String topic) {
        if (config.isReadSource()) {
            return sourceTopicService.getTopicByCode(namespace, topic);
        } else {
            try {
                return targetTopicService.getTopicByCode(namespace, topic);
            } catch (Exception e) {
                logger.error("getTopicByCode exception, namespace: {}, topic: {}", namespace, topic, e);
                return sourceTopicService.getTopicByCode(namespace, topic);
            }
        }
    }

    @Override
    public PageResult<Topic> search(QPageQuery<TopicQuery> pageQuery) {
        if (config.isReadSource()) {
            return sourceTopicService.search(pageQuery);
        } else {
            try {
                return targetTopicService.search(pageQuery);
            } catch (Exception e) {
                logger.error("search exception, pageQuery: {}", pageQuery, e);
                return sourceTopicService.search(pageQuery);
            }
        }
    }

    @Override
    public PageResult<Topic> findUnsubscribedByQuery(QPageQuery<TopicQuery> pageQuery) {
        if (config.isReadSource()) {
            return sourceTopicService.findUnsubscribedByQuery(pageQuery);
        } else {
            try {
                return targetTopicService.findUnsubscribedByQuery(pageQuery);
            } catch (Exception e) {
                logger.error("findUnsubscribedByQuery exception, pageQuery: {}", pageQuery, e);
                return sourceTopicService.findUnsubscribedByQuery(pageQuery);
            }
        }
    }

    @Override
    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {
        if (config.isWriteSource()) {
            sourceTopicService.addTopic(topic, partitionGroups);
        }
        if (config.isWriteTarget()) {
            try {
                targetTopicService.addTopic(topic, partitionGroups);
            } catch (Exception e) {
                logger.error("add exception, params: {}, {}", topic, partitionGroups, e);
            }
        }
    }

    @Override
    public void removeTopic(Topic topic) {
        if (config.isWriteSource()) {
            sourceTopicService.removeTopic(topic);
        }
        if (config.isWriteTarget()) {
            try {
                targetTopicService.removeTopic(topic);
            } catch (Exception e) {
                logger.error("removeTopic exception, params: {}", topic, e);
            }
        }
    }

    @Override
    public void addPartitionGroup(PartitionGroup group) {
        if (config.isWriteSource()) {
            sourceTopicService.addPartitionGroup(group);
        }
        if (config.isWriteTarget()) {
            try {
                targetTopicService.addPartitionGroup(group);
            } catch (Exception e) {
                logger.error("addPartitionGroup exception, params: {}", group, e);
            }
        }
    }

    @Override
    public void removePartitionGroup(PartitionGroup group) {
        if (config.isWriteSource()) {
            sourceTopicService.removePartitionGroup(group);
        }
        if (config.isWriteTarget()) {
            try {
                targetTopicService.removePartitionGroup(group);
            } catch (Exception e) {
                logger.error("removePartitionGroup exception, params: {}", group, e);
            }
        }
    }

    @Override
    public Collection<Integer> updatePartitionGroup(PartitionGroup group) {
        Collection<Integer> result = null;
        if (config.isWriteSource()) {
            result = sourceTopicService.updatePartitionGroup(group);
        }
        if (config.isWriteTarget()) {
            try {
                result = targetTopicService.updatePartitionGroup(group);
            } catch (Exception e) {
                logger.error("updatePartitionGroup exception, params: {}", group, e);
            }
        }
        return result;
    }

    @Override
    public void leaderReport(PartitionGroup group) {
        if (config.isWriteSource()) {
            sourceTopicService.leaderReport(group);
        }
        if (config.isWriteTarget()) {
            try {
                targetTopicService.leaderReport(group);
            } catch (Exception e) {
                logger.error("leaderReport exception, params: {}", group, e);
            }
        }
    }

    @Override
    public void leaderChange(PartitionGroup group) {
        if (config.isWriteSource()) {
            sourceTopicService.leaderChange(group);
        }
        if (config.isWriteTarget()) {
            try {
                targetTopicService.leaderChange(group);
            } catch (Exception e) {
                logger.error("leaderChange exception, params: {}", group, e);
            }
        }
    }

    @Override
    public List<PartitionGroup> getPartitionGroup(String namespace, String topic, Object[] groups) {
        if (config.isReadSource()) {
            return sourceTopicService.getPartitionGroup(namespace, topic, groups);
        } else {
            return targetTopicService.getPartitionGroup(namespace, topic, groups);
        }
    }

    @Override
    public Topic add(Topic topic) {
        Topic result = null;
        if (config.isWriteSource()) {
            result = sourceTopicService.add(topic);
        }
        if (config.isWriteTarget()) {
            try {
                targetTopicService.add(topic);
            } catch (Exception e) {
                logger.error("add exception, params: {}", topic, e);
            }
        }
        return result;
    }

    @Override
    public Topic update(Topic topic) {
        Topic result = null;
        if (config.isWriteSource()) {
            result = sourceTopicService.update(topic);
        }
        if (config.isWriteTarget()) {
            try {
                targetTopicService.update(topic);
            } catch (Exception e) {
                logger.error("update exception, params: {}", topic, e);
            }
        }
        return result;
    }

    @Override
    public Topic getById(String id) {
        if (config.isReadSource()) {
            return sourceTopicService.getById(id);
        } else {
            try {
                return targetTopicService.getById(id);
            } catch (Exception e) {
                logger.error("getById exception, id: {}", id, e);
                return sourceTopicService.getById(id);
            }
        }
    }

    @Override
    public List<Topic> getAll() {
        if (config.isReadSource()) {
            return sourceTopicService.getAll();
        } else {
            try {
                return targetTopicService.getAll();
            } catch (Exception e) {
                logger.error("getAll exception", e);
                return sourceTopicService.getAll();
            }
        }
    }
}