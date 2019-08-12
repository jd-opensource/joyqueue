package com.jd.joyqueue.nsr.composition.service;

import com.jd.joyqueue.nsr.composition.config.CompositionConfig;
import com.jd.joyqueue.domain.PartitionGroup;
import com.jd.joyqueue.domain.Topic;
import com.jd.joyqueue.model.PageResult;
import com.jd.joyqueue.model.QPageQuery;
import com.jd.joyqueue.nsr.model.TopicQuery;
import com.jd.joyqueue.nsr.service.TopicService;

import java.util.Collection;
import java.util.List;

/**
 * CompositionTopicService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionTopicService implements TopicService {

    private CompositionConfig config;
    private TopicService igniteTopicService;
    private TopicService journalkeeperTopicService;

    public CompositionTopicService(CompositionConfig config, TopicService igniteTopicService, TopicService journalkeeperTopicService) {
        this.config = config;
        this.igniteTopicService = igniteTopicService;
        this.journalkeeperTopicService = journalkeeperTopicService;
    }

    @Override
    public Topic getTopicByCode(String namespace, String topic) {
        return null;
    }

    @Override
    public PageResult<Topic> findUnsubscribedByQuery(QPageQuery<TopicQuery> pageQuery) {
        return null;
    }

    @Override
    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {

    }

    @Override
    public void removeTopic(Topic topic) {

    }

    @Override
    public void addPartitionGroup(PartitionGroup group) {

    }

    @Override
    public void removePartitionGroup(PartitionGroup group) {

    }

    @Override
    public Collection<Integer> updatePartitionGroup(PartitionGroup group) {
        return null;
    }

    @Override
    public void leaderChange(PartitionGroup group) {

    }

    @Override
    public List<PartitionGroup> getPartitionGroup(String namesapce, String topic, Object[] groups) {
        return null;
    }

    @Override
    public Topic getById(String id) {
        return null;
    }

    @Override
    public Topic get(Topic model) {
        return null;
    }

    @Override
    public void addOrUpdate(Topic topic) {

    }

    @Override
    public void deleteById(String id) {

    }

    @Override
    public void delete(Topic model) {

    }

    @Override
    public List<Topic> list() {
        return null;
    }

    @Override
    public List<Topic> list(TopicQuery query) {
        return null;
    }

    @Override
    public PageResult<Topic> pageQuery(QPageQuery<TopicQuery> pageQuery) {
        return null;
    }
}