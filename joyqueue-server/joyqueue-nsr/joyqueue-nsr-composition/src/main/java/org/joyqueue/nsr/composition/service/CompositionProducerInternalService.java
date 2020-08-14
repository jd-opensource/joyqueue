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

import org.joyqueue.domain.Producer;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.composition.config.CompositionConfig;
import org.joyqueue.nsr.service.internal.ProducerInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CompositionProducerInternalService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionProducerInternalService implements ProducerInternalService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionProducerInternalService.class);

    private CompositionConfig config;
    private ProducerInternalService sourceProducerService;
    private ProducerInternalService targetProducerService;

    public CompositionProducerInternalService(CompositionConfig config, ProducerInternalService sourceProducerService,
                                              ProducerInternalService targetProducerService) {
        this.config = config;
        this.sourceProducerService = sourceProducerService;
        this.targetProducerService = targetProducerService;
    }

    @Override
    public Producer getById(String id) {
        if (config.isReadSource()) {
            return sourceProducerService.getById(id);
        } else {
            try {
                return targetProducerService.getById(id);
            } catch (Exception e) {
                logger.error("getById exception, id: {}", id, e);
                return sourceProducerService.getById(id);
            }
        }
    }

    @Override
    public Producer getByTopicAndApp(TopicName topic, String app) {
        if (config.isReadSource()) {
            return sourceProducerService.getByTopicAndApp(topic, app);
        } else {
            try {
                return targetProducerService.getByTopicAndApp(topic, app);
            } catch (Exception e) {
                logger.error("getByTopicAndApp exception, topic: {}, app: {}", topic, app, e);
                return sourceProducerService.getByTopicAndApp(topic, app);
            }
        }
    }

    @Override
    public List<Producer> getByTopic(TopicName topic) {
        if (config.isReadSource()) {
            return sourceProducerService.getByTopic(topic);
        } else {
            try {
                return targetProducerService.getByTopic(topic);
            } catch (Exception e) {
                logger.error("getByTopic exception, topic: {}", topic, e);
                return sourceProducerService.getByTopic(topic);
            }
        }
    }

    @Override
    public List<Producer> getByApp(String app) {
        if (config.isReadSource()) {
            return sourceProducerService.getByApp(app);
        } else {
            try {
                return targetProducerService.getByApp(app);
            } catch (Exception e) {
                logger.error("getByApp exception, app: {}", app, e);
                return sourceProducerService.getByApp(app);
            }
        }
    }

    @Override
    public List<Producer> getAll() {
        if (config.isReadSource()) {
            return sourceProducerService.getAll();
        } else {
            try {
                return targetProducerService.getAll();
            } catch (Exception e) {
                logger.error("getAll exception", e);
                return sourceProducerService.getAll();
            }
        }
    }

    @Override
    public Producer add(Producer producer) {
        Producer result = null;
        if (config.isWriteSource()) {
            result = sourceProducerService.add(producer);
        }
        if (config.isWriteTarget()) {
            try {
                targetProducerService.add(producer);
            } catch (Exception e) {
                logger.error("add exception, params: {}", producer, e);
            }
        }
        return result;
    }

    @Override
    public Producer update(Producer producer) {
        Producer result = null;
        if (config.isWriteSource()) {
            result = sourceProducerService.update(producer);
        }
        if (config.isWriteTarget()) {
            try {
                targetProducerService.update(producer);
            } catch (Exception e) {
                logger.error("update exception, params: {}", producer, e);
            }
        }
        return result;
    }

    @Override
    public void delete(String id) {
        if (config.isWriteSource()) {
            sourceProducerService.delete(id);
        }
        if (config.isWriteTarget()) {
            try {
                targetProducerService.delete(id);
            } catch (Exception e) {
                logger.error("delete exception, params: {}", id, e);
            }
        }
    }
}
