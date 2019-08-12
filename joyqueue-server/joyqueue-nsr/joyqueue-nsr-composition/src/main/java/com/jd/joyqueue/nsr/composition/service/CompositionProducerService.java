package com.jd.joyqueue.nsr.composition.service;

import com.jd.joyqueue.nsr.composition.config.CompositionConfig;
import com.jd.joyqueue.domain.Producer;
import com.jd.joyqueue.domain.TopicName;
import com.jd.joyqueue.model.PageResult;
import com.jd.joyqueue.model.QPageQuery;
import com.jd.joyqueue.nsr.model.ProducerQuery;
import com.jd.joyqueue.nsr.service.ProducerService;

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
