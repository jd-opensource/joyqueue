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

import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.composition.config.CompositionConfig;
import org.joyqueue.nsr.service.internal.ConsumerInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionConsumerInternalService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionConsumerInternalService implements ConsumerInternalService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionConsumerInternalService.class);

    private CompositionConfig config;
    private ConsumerInternalService igniteConsumerService;
    private ConsumerInternalService journalkeeperConsumerService;

    public CompositionConsumerInternalService(CompositionConfig config, ConsumerInternalService igniteConsumerService,
                                              ConsumerInternalService journalkeeperConsumerService) {
        this.config = config;
        this.igniteConsumerService = igniteConsumerService;
        this.journalkeeperConsumerService = journalkeeperConsumerService;
    }

    @Override
    public Consumer getByTopicAndApp(TopicName topic, String app) {
        if (config.isReadIgnite()) {
            return igniteConsumerService.getByTopicAndApp(topic, app);
        } else {
            try {
                return journalkeeperConsumerService.getByTopicAndApp(topic, app);
            } catch (Exception e) {
                logger.error("getByTopicAndApp exception, topic: {}, app: {}", topic, app, e);
                return igniteConsumerService.getByTopicAndApp(topic, app);
            }
        }
    }

    @Override
    public List<Consumer> getByTopic(TopicName topic) {
        if (config.isReadIgnite()) {
            return igniteConsumerService.getByTopic(topic);
        } else {
            try {
                return journalkeeperConsumerService.getByTopic(topic);
            } catch (Exception e) {
                logger.error("getByTopic exception, topic: {}", topic, e);
                return igniteConsumerService.getByTopic(topic);
            }
        }
    }

    @Override
    public List<Consumer> getByApp(String app) {
        if (config.isReadIgnite()) {
            return igniteConsumerService.getByApp(app);
        } else {
            try {
                return journalkeeperConsumerService.getByApp(app);
            } catch (Exception e) {
                logger.error("getByApp exception, app: {}", app, e);
                return igniteConsumerService.getByApp(app);
            }
        }
    }

    @Override
    public List<Consumer> getAll() {
        if (config.isReadIgnite()) {
            return igniteConsumerService.getAll();
        } else {
            try {
                return journalkeeperConsumerService.getAll();
            } catch (Exception e) {
                logger.error("getAll exception", e);
                return igniteConsumerService.getAll();
            }
        }
    }

    @Override
    public Consumer add(Consumer consumer) {
        Consumer result = null;
        if (config.isWriteIgnite()) {
            result = igniteConsumerService.add(consumer);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperConsumerService.add(consumer);
            } catch (Exception e) {
                logger.error("add journalkeeper exception, params: {}", consumer, e);
            }
        }
        return result;
    }

    @Override
    public Consumer update(Consumer consumer) {
        Consumer result = null;
        if (config.isWriteIgnite()) {
            result = igniteConsumerService.update(consumer);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperConsumerService.update(consumer);
            } catch (Exception e) {
                logger.error("update journalkeeper exception, params: {}", consumer, e);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        if (config.isWriteIgnite()) {
            igniteConsumerService.delete(id);
        }
        if (config.isWriteJournalkeeper()) {
            try {
                journalkeeperConsumerService.delete(id);
            } catch (Exception e) {
                logger.error("delete journalkeeper exception, params: {}", id, e);
            }
        }
    }

    @Override
    public Consumer getById(String id) {
        if (config.isReadIgnite()) {
            return igniteConsumerService.getById(id);
        } else {
            try {
                return journalkeeperConsumerService.getById(id);
            } catch (Exception e) {
                logger.error("getById exception, id: {}", id, e);
                return igniteConsumerService.getById(id);
            }
        }
    }
}
