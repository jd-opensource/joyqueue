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

import org.joyqueue.domain.Producer;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.sql.converter.ProducerConverter;
import org.joyqueue.nsr.sql.repository.ProducerRepository;
import org.joyqueue.nsr.service.internal.ProducerInternalService;

import java.util.List;

/**
 * SQLProducerInternalService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class SQLProducerInternalService implements ProducerInternalService {

    private ProducerRepository producerRepository;

    public SQLProducerInternalService(ProducerRepository producerRepository) {
        this.producerRepository = producerRepository;
    }

    @Override
    public Producer getById(String id) {
        return ProducerConverter.convert(producerRepository.getById(id));
    }

    @Override
    public Producer getByTopicAndApp(TopicName topic, String app) {
        return ProducerConverter.convert(producerRepository.getByTopicAndApp(topic.getCode(), topic.getNamespace(), app));
    }

    @Override
    public List<Producer> getByTopic(TopicName topic) {
        return ProducerConverter.convert(producerRepository.getByTopic(topic.getCode(), topic.getNamespace()));
    }

    @Override
    public List<Producer> getByApp(String app) {
        return ProducerConverter.convert(producerRepository.getByApp(app));
    }

    @Override
    public List<Producer> getAll() {
        return ProducerConverter.convert(producerRepository.getAll());
    }

    @Override
    public Producer add(Producer producer) {
        return ProducerConverter.convert(producerRepository.add(ProducerConverter.convert(producer)));
    }

    @Override
    public Producer update(Producer producer) {
        return ProducerConverter.convert(producerRepository.update(ProducerConverter.convert(producer)));
    }

    @Override
    public void delete(String id) {
        producerRepository.deleteById(id);
    }
}