package io.chubao.joyqueue.nsr.journalkeeper.service;

import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Topic;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.journalkeeper.TransactionContext;
import io.chubao.joyqueue.nsr.journalkeeper.converter.PartitionGroupConverter;
import io.chubao.joyqueue.nsr.journalkeeper.converter.TopicConverter;
import io.chubao.joyqueue.nsr.journalkeeper.repository.PartitionGroupRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.TopicRepository;
import io.chubao.joyqueue.nsr.journalkeeper.domain.TopicDTO;
import io.chubao.joyqueue.nsr.model.TopicQuery;
import io.chubao.joyqueue.nsr.service.TopicService;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.List;

/**
 * JournalkeeperTopicService
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class JournalkeeperTopicService implements TopicService {

    private TopicRepository topicRepository;
    private PartitionGroupRepository partitionGroupRepository;

    public JournalkeeperTopicService(TopicRepository topicRepository, PartitionGroupRepository partitionGroupRepository) {
        this.topicRepository = topicRepository;
        this.partitionGroupRepository = partitionGroupRepository;
    }

    @Override
    public Topic getTopicByCode(String namespace, String topic) {
        return TopicConverter.convert(topicRepository.getByCodeAndNamespace(topic, namespace));
    }

    @Override
    public PageResult<Topic> findUnsubscribedByQuery(QPageQuery<TopicQuery> pageQuery) {
        return null;
    }

    @Override
    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {
        TransactionContext.beginTransaction();

        try {
            TopicDTO topicDTO = TopicConverter.convert(topic);
            topicRepository.add(topicDTO);

            if (CollectionUtils.isNotEmpty(partitionGroups)) {
                for (PartitionGroup partitionGroup : partitionGroups) {
                    partitionGroupRepository.add(PartitionGroupConverter.convert(partitionGroup));
                }
            }
            TransactionContext.commit();
        } catch (Exception e) {
            TransactionContext.rollback();
            throw e;
        }
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
        return TopicConverter.convert(topicRepository.getById(id));
    }

    @Override
    public Topic get(Topic model) {
        return null;
    }

    @Override
    public void addOrUpdate(Topic topic) {
        addTopic(topic, null);
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