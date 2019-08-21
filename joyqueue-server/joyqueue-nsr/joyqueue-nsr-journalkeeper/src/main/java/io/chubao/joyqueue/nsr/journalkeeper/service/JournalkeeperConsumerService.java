package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.journalkeeper.converter.ConsumerConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.ConsumerRepository;
import io.chubao.joyqueue.nsr.service.ConsumerService;

import java.util.List;

/**
 * JournalkeeperConsumerService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JournalkeeperConsumerService implements ConsumerService {

    private ConsumerRepository consumerRepository;

    public JournalkeeperConsumerService(ConsumerRepository consumerRepository) {
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
        return ConsumerConverter.convert(consumerRepository.add(ConsumerConverter.convert(consumer)));
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