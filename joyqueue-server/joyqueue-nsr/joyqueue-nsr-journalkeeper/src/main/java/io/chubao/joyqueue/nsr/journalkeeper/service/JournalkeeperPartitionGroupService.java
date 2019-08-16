package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.journalkeeper.converter.PartitionGroupConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.PartitionGroupRepository;
import io.chubao.joyqueue.nsr.model.PartitionGroupQuery;
import io.chubao.joyqueue.nsr.service.PartitionGroupService;

import java.util.List;

/**
 * JournalkeeperPartitionGroupService
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class JournalkeeperPartitionGroupService implements PartitionGroupService {

    private PartitionGroupRepository partitionGroupRepository;

    public JournalkeeperPartitionGroupService(PartitionGroupRepository partitionGroupRepository) {
        this.partitionGroupRepository = partitionGroupRepository;
    }

    @Override
    public PartitionGroup findByTopicAndGroup(TopicName topic, int group) {
        return PartitionGroupConverter.convert(partitionGroupRepository.getByTopicAndGroup(topic.getCode(), topic.getNamespace(), group));
    }

    @Override
    public List<PartitionGroup> getByTopic(TopicName topic) {
        return PartitionGroupConverter.convert(partitionGroupRepository.getByTopic(topic.getCode(), topic.getNamespace()));
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