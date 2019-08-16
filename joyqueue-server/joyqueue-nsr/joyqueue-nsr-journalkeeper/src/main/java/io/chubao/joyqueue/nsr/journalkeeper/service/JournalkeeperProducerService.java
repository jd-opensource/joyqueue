package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.journalkeeper.converter.ProducerConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.ProducerRepository;
import io.chubao.joyqueue.nsr.model.ProducerQuery;
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
    public void deleteByTopicAndApp(TopicName topic, String app) {

    }

    @Override
    public Producer getByTopicAndApp(TopicName topic, String app) {
        return ProducerConverter.convert(producerRepository.getByTopicAndApp(topic.getCode(), topic.getNamespace(), app));
    }

    @Override
    public List<Producer> getByTopic(TopicName topic, boolean withConfig) {
        return ProducerConverter.convert(producerRepository.getByTopic(topic.getCode(), topic.getNamespace()));
    }

    @Override
    public List<Producer> getByApp(String app, boolean withConfig) {
        return ProducerConverter.convert(producerRepository.getByApp(app));
    }

    @Override
    public void add(Producer producer) {
        producerRepository.add(ProducerConverter.convert(producer));
    }

    @Override
    public void update(Producer producer) {

    }

    @Override
    public void remove(Producer producer) {

    }

    @Override
    public List<Producer> getProducerByClientType(byte clientType) {
        return null;
    }

    @Override
    public Producer getById(String id) {
        return null;
    }

    @Override
    public Producer get(Producer model) {
        return null;
    }

    @Override
    public void addOrUpdate(Producer producer) {
        add(producer);
    }

    @Override
    public void deleteById(String id) {

    }

    @Override
    public void delete(Producer model) {

    }

    @Override
    public List<Producer> list() {
        return null;
    }

    @Override
    public List<Producer> list(ProducerQuery query) {
        return null;
    }

    @Override
    public PageResult<Producer> pageQuery(QPageQuery<ProducerQuery> pageQuery) {
        return null;
    }
}