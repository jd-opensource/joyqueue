package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.journalkeeper.converter.ConsumerConverter;
import io.chubao.joyqueue.nsr.journalkeeper.domain.ConsumerDTO;
import io.chubao.joyqueue.nsr.journalkeeper.repository.ConsumerRepository;
import io.chubao.joyqueue.nsr.service.internal.ConsumerInternalService;

import java.util.List;

/**
 * JournalkeeperConsumerInternalService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JournalkeeperConsumerInternalService implements ConsumerInternalService {

    private ConsumerRepository consumerRepository;

    public JournalkeeperConsumerInternalService(ConsumerRepository consumerRepository) {
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
        consumerDTO.setReferer(consumerDTO.getApp().split("\\.")[0]);
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