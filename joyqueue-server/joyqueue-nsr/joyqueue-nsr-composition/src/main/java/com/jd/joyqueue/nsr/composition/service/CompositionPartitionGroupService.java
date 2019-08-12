package com.jd.joyqueue.nsr.composition.service;

import com.jd.joyqueue.nsr.composition.config.CompositionConfig;
import com.jd.joyqueue.domain.PartitionGroup;
import com.jd.joyqueue.domain.TopicName;
import com.jd.joyqueue.model.PageResult;
import com.jd.joyqueue.model.QPageQuery;
import com.jd.joyqueue.nsr.model.PartitionGroupQuery;
import com.jd.joyqueue.nsr.service.PartitionGroupService;

import java.util.List;

/**
 * CompositionPartitionGroupService
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public class CompositionPartitionGroupService implements PartitionGroupService {

    private CompositionConfig config;
    private PartitionGroupService ignitePartitionGroupService;
    private PartitionGroupService journalkeeperPartitionGroupService;

    public CompositionPartitionGroupService(CompositionConfig config, PartitionGroupService ignitePartitionGroupService,
                                            PartitionGroupService journalkeeperPartitionGroupService) {
        this.config = config;
        this.ignitePartitionGroupService = ignitePartitionGroupService;
        this.journalkeeperPartitionGroupService = journalkeeperPartitionGroupService;
    }

    @Override
    public PartitionGroup findByTopicAndGroup(TopicName topic, int group) {
        return null;
    }

    @Override
    public List<PartitionGroup> getByTopic(TopicName topic) {
        return null;
    }

    @Override
    public PartitionGroup getById(String id) {
        return null;
    }

    @Override
    public PartitionGroup get(PartitionGroup model) {
        return null;
    }

    @Override
    public void addOrUpdate(PartitionGroup partitionGroup) {

    }

    @Override
    public void deleteById(String id) {

    }

    @Override
    public void delete(PartitionGroup model) {

    }

    @Override
    public List<PartitionGroup> list() {
        return null;
    }

    @Override
    public List<PartitionGroup> list(PartitionGroupQuery query) {
        return null;
    }

    @Override
    public PageResult<PartitionGroup> pageQuery(QPageQuery<PartitionGroupQuery> pageQuery) {
        return null;
    }
}
