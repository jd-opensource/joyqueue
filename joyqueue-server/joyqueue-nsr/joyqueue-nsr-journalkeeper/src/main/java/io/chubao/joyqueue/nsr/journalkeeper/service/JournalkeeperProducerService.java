package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.journalkeeper.converter.ProducerConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.ProducerRepository;
import io.chubao.joyqueue.nsr.service.ProducerService;

import java.util.List;

/**
 * JournalkeeperProducerService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JournalkeeperProducerService implements ProducerService {

    private ProducerRepository producerRepository;

    public JournalkeeperProducerService(ProducerRepository producerRepository) {
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