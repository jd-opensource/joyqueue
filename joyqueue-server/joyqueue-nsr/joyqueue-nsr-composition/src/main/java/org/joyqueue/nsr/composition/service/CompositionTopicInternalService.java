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
    private TopicInternalService igniteTopicService;
    private TopicInternalService journalkeeperTopicService;

    public CompositionTopicInternalService(CompositionConfig config, TopicInternalService igniteTopicService, TopicInternalService journalkeeperTopicService) {
        this.config = config;
        this.igniteTopicService = igniteTopicService;
        this.journalkeeperTopicService = journalkeeperTopicService;
    }

    @Override
    public Topic getTopicByCode(String namespace, String topic) {
        if (config.isReadIgnite()) {
            return igniteTopicService.getTopicByCode(namespace, topic);
        } else {
            try {
                return journalkeeperTopicService.getTopicByCode(namespace, topic);
            } catch (Exception e) {
                logger.error("getTopicByCode exception, namespace: {}, topic: {}", namespace, topic, e);
                return igniteTopicService.getTopicByCode(namespace, topic);
            }
        }
    }

    @Override
    public PageResult<Topic> search(QPageQuery<TopicQuery> pageQuery) {
        if (config.isReadIgnite()) {
            return igniteTopicService.search(pageQuery);
        } else {
            try {
                return journalkeeperTopicService.search(pageQuery);
            } catch (Exception e) {
                logger.error("search exception, pageQuery: {}", pageQuery, e);
                return igniteTopicService.search(pageQuery);
            }
        }
    }

    @Override
    public PageResult<Topic> findUnsubscribedByQuery(QPageQuery<TopicQuery> pageQuery) {
        if (config.isReadIgnite()) {
            return igniteTopicService.findUnsubscribedByQuery(pageQuery);
        } else {
            try {
                return journalkeeperTopicService.findUnsubscribedByQuery(pageQuery);
            } catch (Exception e) {
                logger.error("findUnsubscribedByQuery exception, pageQuery: {}", pageQuery, e);
                return igniteTopicService.findUnsubscribedByQuery(pageQuery);
            }
        }
    }

    @Override
    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {
        if (config.isWriteIgnite()) {
            igniteTopicService.addTopic(topic, partitionGroups);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTopicService.addTopic(topic, partitionGroups);
            } catch (Exception e) {
                logger.error("add journalkeeper exception, params: {}, {}", topic, partitionGroups, e);
            }
        }
    }

    @Override
    public void removeTopic(Topic topic) {
        if (config.isWriteIgnite()) {
            igniteTopicService.removeTopic(topic);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTopicService.removeTopic(topic);
            } catch (Exception e) {
                logger.error("removeTopic journalkeeper exception, params: {}", topic, e);
            }
        }
    }

    @Override
    public void addPartitionGroup(PartitionGroup group) {
        if (config.isWriteIgnite()) {
            igniteTopicService.addPartitionGroup(group);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTopicService.addPartitionGroup(group);
            } catch (Exception e) {
                logger.error("addPartitionGroup journalkeeper exception, params: {}", group, e);
            }
        }
    }

    @Override
    public void removePartitionGroup(PartitionGroup group) {
        if (config.isWriteIgnite()) {
            igniteTopicService.removePartitionGroup(group);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTopicService.removePartitionGroup(group);
            } catch (Exception e) {
                logger.error("removePartitionGroup journalkeeper exception, params: {}", group, e);
            }
        }
    }

    @Override
    public Collection<Integer> updatePartitionGroup(PartitionGroup group) {
        Collection<Integer> result = null;
        if (config.isWriteIgnite()) {
            result = igniteTopicService.updatePartitionGroup(group);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                result = journalkeeperTopicService.updatePartitionGroup(group);
            } catch (Exception e) {
                logger.error("updatePartitionGroup journalkeeper exception, params: {}", group, e);
            }
        }
        return result;
    }

    @Override
    public void leaderReport(PartitionGroup group) {
        if (config.isWriteIgnite()) {
            igniteTopicService.leaderReport(group);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTopicService.leaderReport(group);
            } catch (Exception e) {
                logger.error("leaderReport journalkeeper exception, params: {}", group, e);
            }
        }
    }

    @Override
    public void leaderChange(PartitionGroup group) {
        if (config.isWriteIgnite()) {
            igniteTopicService.leaderChange(group);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTopicService.leaderChange(group);
            } catch (Exception e) {
                logger.error("leaderChange journalkeeper exception, params: {}", group, e);
            }
        }
    }

    @Override
    public List<PartitionGroup> getPartitionGroup(String namespace, String topic, Object[] groups) {
        if (config.isReadIgnite()) {
            return igniteTopicService.getPartitionGroup(namespace, topic, groups);
        } else {
            return journalkeeperTopicService.getPartitionGroup(namespace, topic, groups);
        }
    }

    @Override
    public Topic add(Topic topic) {
        Topic result = null;
        if (config.isWriteIgnite()) {
            result = igniteTopicService.add(topic);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTopicService.add(topic);
            } catch (Exception e) {
                logger.error("add journalkeeper exception, params: {}", topic, e);
            }
        }
        return result;
    }

    @Override
    public Topic update(Topic topic) {
        Topic result = null;
        if (config.isWriteIgnite()) {
            result = igniteTopicService.update(topic);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperTopicService.update(topic);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", topic, e);
            }
        }
        return result;
    }

    @Override
    public Topic getById(String id) {
        if (config.isReadIgnite()) {
            return igniteTopicService.getById(id);
        } else {
            try {
                return journalkeeperTopicService.getById(id);
            } catch (Exception e) {
                logger.error("getById exception, id: {}", id, e);
                return igniteTopicService.getById(id);
            }
        }
    }

    @Override
    public List<Topic> getAll() {
        if (config.isReadIgnite()) {
            return igniteTopicService.getAll();
        } else {
            try {
                return journalkeeperTopicService.getAll();
            } catch (Exception e) {
                logger.error("getAll exception", e);
                return igniteTopicService.getAll();
            }
        }
    }
}