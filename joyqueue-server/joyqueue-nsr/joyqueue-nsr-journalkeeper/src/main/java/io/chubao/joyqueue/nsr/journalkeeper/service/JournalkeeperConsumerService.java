package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.journalkeeper.converter.ConsumerConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.ConsumerRepository;
import io.chubao.joyqueue.nsr.model.ConsumerQuery;
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
    public void deleteByTopicAndApp(TopicName topic, String app) {

    }

    @Override
    public Consumer getByTopicAndApp(TopicName topic, String app) {
        return ConsumerConverter.convert(consumerRepository.getByTopicAndApp(topic.getCode(), topic.getNamespace(), app));
    }

    @Override
    public List<Consumer> getByTopic(TopicName topic, boolean withConfig) {
        return ConsumerConverter.convert(consumerRepository.getByTopic(topic.getCode(), topic.getNamespace()));
    }

    @Override
    public List<Consumer> getByApp(String app, boolean withConfig) {
        return ConsumerConverter.convert(consumerRepository.getByApp(app));
    }

    @Override
    public List<Consumer> getConsumerByClientType(byte clientType) {
        return null;
    }

    @Override
    public void add(Consumer consumer) {
        consumerRepository.add(ConsumerConverter.convert(consumer));
    }

    @Override
    public void update(Consumer consumer) {

    }

    @Override
    public void remove(Consumer consumer) {

    }

    @Override
    public Consumer getById(String id) {
        return null;
    }

    @Override
    public Consumer get(Consumer model) {
        return null;
    }

    @Override
    public void addOrUpdate(Consumer consumer) {
        add(consumer);
    }

    @Override
    public void deleteById(String id) {

    }

    @Override
    public void delete(Consumer model) {

    }

    @Override
    public List<Consumer> list() {
        return null;
    }

    @Override
    public List<Consumer> list(ConsumerQuery query) {
        return null;
    }

    @Override
    public PageResult<Consumer> pageQuery(QPageQuery<ConsumerQuery> pageQuery) {
        return null;
    }
}