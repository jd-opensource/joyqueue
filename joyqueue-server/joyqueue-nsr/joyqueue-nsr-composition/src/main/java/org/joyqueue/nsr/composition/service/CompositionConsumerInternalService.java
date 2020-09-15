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
    private ConsumerInternalService sourceConsumerService;
    private ConsumerInternalService targetConsumerService;

    public CompositionConsumerInternalService(CompositionConfig config, ConsumerInternalService sourceConsumerService,
                                              ConsumerInternalService targetConsumerService) {
        this.config = config;
        this.sourceConsumerService = sourceConsumerService;
        this.targetConsumerService = targetConsumerService;
    }

    @Override
    public Consumer getByTopicAndApp(TopicName topic, String app) {
        if (config.isReadSource()) {
            return sourceConsumerService.getByTopicAndApp(topic, app);
        } else {
            try {
                return targetConsumerService.getByTopicAndApp(topic, app);
            } catch (Exception e) {
                logger.error("getByTopicAndApp exception, topic: {}, app: {}", topic, app, e);
                return sourceConsumerService.getByTopicAndApp(topic, app);
            }
        }
    }

    @Override
    public List<Consumer> getByTopic(TopicName topic) {
        if (config.isReadSource()) {
            return sourceConsumerService.getByTopic(topic);
        } else {
            try {
                return targetConsumerService.getByTopic(topic);
            } catch (Exception e) {
                logger.error("getByTopic exception, topic: {}", topic, e);
                return sourceConsumerService.getByTopic(topic);
            }
        }
    }

    @Override
    public List<Consumer> getByApp(String app) {
        if (config.isReadSource()) {
            return sourceConsumerService.getByApp(app);
        } else {
            try {
                return targetConsumerService.getByApp(app);
            } catch (Exception e) {
                logger.error("getByApp exception, app: {}", app, e);
                return sourceConsumerService.getByApp(app);
            }
        }
    }

    @Override
    public List<Consumer> getAll() {
        if (config.isReadSource()) {
            return sourceConsumerService.getAll();
        } else {
            try {
                return targetConsumerService.getAll();
            } catch (Exception e) {
                logger.error("getAll exception", e);
                return sourceConsumerService.getAll();
            }
        }
    }

    @Override
    public Consumer add(Consumer consumer) {
        Consumer result = null;
        if (config.isWriteSource()) {
            result = sourceConsumerService.add(consumer);
        }
        if (config.isWriteTarget()) {
            try {
                targetConsumerService.add(consumer);
            } catch (Exception e) {
                logger.error("add exception, params: {}", consumer, e);
            }
        }
        return result;
    }

    @Override
    public Consumer update(Consumer consumer) {
        Consumer result = null;
        if (config.isWriteSource()) {
            result = sourceConsumerService.update(consumer);
        }
        if (config.isWriteTarget()) {
            try {
                targetConsumerService.update(consumer);
            } catch (Exception e) {
                logger.error("update exception, params: {}", consumer, e);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        if (config.isWriteSource()) {
            sourceConsumerService.delete(id);
        }
        if (config.isWriteTarget()) {
            try {
                targetConsumerService.delete(id);
            } catch (Exception e) {
                logger.error("delete exception, params: {}", id, e);
            }
        }
    }

    @Override
    public Consumer getById(String id) {
        if (config.isReadSource()) {
            return sourceConsumerService.getById(id);
        } else {
            try {
                return targetConsumerService.getById(id);
            } catch (Exception e) {
                logger.error("getById exception, id: {}", id, e);
                return sourceConsumerService.getById(id);
            }
        }
    }
}
