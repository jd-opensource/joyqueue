package io.chubao.joyqueue.nsr.composition.service;

import io.chubao.joyqueue.nsr.composition.config.CompositionConfig;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.model.ProducerQuery;
import io.chubao.joyqueue.nsr.service.ProducerService;

import java.util.List;

/**
 * CompositionProducerService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionProducerService implements ProducerService {

    private CompositionConfig config;
    private ProducerService igniteProducerService;
    private ProducerService journalkeeperProducerService;

    public CompositionProducerService(CompositionConfig config, ProducerService igniteProducerService,
                                      ProducerService journalkeeperProducerService) {
        this.config = config;
        this.igniteProducerService = igniteProducerService;
        this.journalkeeperProducerService = journalkeeperProducerService;
    }

    @Override
    public void deleteByTopicAndApp(TopicName topic, String app) {

    }

    @Override
    public Producer getByTopicAndApp(TopicName topic, String app) {
        return null;
    }

    @Override
    public List<Producer> getByTopic(TopicName topic, boolean withConfig) {
        return null;
    }

    @Override
    public List<Producer> getByApp(String app, boolean withConfig) {
        return null;
    }

    @Override
    public void add(Producer producer) {

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
