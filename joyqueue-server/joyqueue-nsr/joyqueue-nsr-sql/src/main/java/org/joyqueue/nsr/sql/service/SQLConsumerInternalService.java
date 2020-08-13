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

import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.sql.converter.ConsumerConverter;
import org.joyqueue.nsr.sql.domain.ConsumerDTO;
import org.joyqueue.nsr.sql.repository.ConsumerRepository;
import org.joyqueue.nsr.service.internal.ConsumerInternalService;

import java.util.List;

/**
 * SQLConsumerInternalService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class SQLConsumerInternalService implements ConsumerInternalService {

    private ConsumerRepository consumerRepository;

    public SQLConsumerInternalService(ConsumerRepository consumerRepository) {
        this.consumerRepository = consumerRepository;
    }

    @Override
    public Consumer getById(String id) {
        return ConsumerConverter.convert(consumerRepository.getById(id));
    }

    @Override
    public Consumer getByTopicAndApp(TopicName topic, String app) {
        return ConsumerConverter.convert(consumerRepository.getByTopicAndApp(topic.getCode(), topic.getNamespace(), app));
    }

    @Override
    public List<Consumer> getByTopic(TopicName topic) {
        return ConsumerConverter.convert(consumerRepository.getByTopic(topic.getCode(), topic.getNamespace()));
    }

    @Override
    public List<Consumer> getByApp(String app) {
        return ConsumerConverter.convert(consumerRepository.getByApp(app));
    }

    @Override
    public List<Consumer> getAll() {
        return ConsumerConverter.convert(consumerRepository.getAll());
    }

    @Override
    public Consumer add(Consumer consumer) {
        // TODO group处理
        ConsumerDTO consumerDTO = ConsumerConverter.convert(consumer);
        return ConsumerConverter.convert(consumerRepository.add(consumerDTO));
    }

    @Override
    public Consumer update(Consumer consumer) {
        return ConsumerConverter.convert(consumerRepository.update(ConsumerConverter.convert(consumer)));
    }

    @Override
    public void delete(String id) {
        consumerRepository.deleteById(id);
    }
}