package io.chubao.joyqueue.nsr.journalkeeper.service;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Topic;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.exception.NsrException;
import io.chubao.joyqueue.nsr.journalkeeper.TransactionContext;
import io.chubao.joyqueue.nsr.journalkeeper.converter.PartitionGroupConverter;
import io.chubao.joyqueue.nsr.journalkeeper.converter.TopicConverter;
import io.chubao.joyqueue.nsr.journalkeeper.domain.PartitionGroupDTO;
import io.chubao.joyqueue.nsr.journalkeeper.domain.PartitionGroupReplicaDTO;
import io.chubao.joyqueue.nsr.journalkeeper.domain.TopicDTO;
import io.chubao.joyqueue.nsr.journalkeeper.repository.PartitionGroupReplicaRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.PartitionGroupRepository;
import io.chubao.joyqueue.nsr.journalkeeper.repository.TopicRepository;
import io.chubao.joyqueue.nsr.model.TopicQuery;
import io.chubao.joyqueue.nsr.service.TopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * JournalkeeperTopicService
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class JournalkeeperTopicService implements TopicService {

    protected static final Logger logger = LoggerFactory.getLogger(JournalkeeperTopicService.class);

    private TopicRepository topicRepository;
    private PartitionGroupRepository partitionGroupRepository;
    private PartitionGroupReplicaRepository partitionGroupReplicaRepository;

    public JournalkeeperTopicService(TopicRepository topicRepository, PartitionGroupRepository partitionGroupRepository, PartitionGroupReplicaRepository partitionGroupReplicaRepository) {
        this.topicRepository = topicRepository;
        this.partitionGroupRepository = partitionGroupRepository;
        this.partitionGroupReplicaRepository = partitionGroupReplicaRepository;
    }

    @Override
    public Topic getById(String id) {
        return TopicConverter.convert(topicRepository.getById(id));
    }

    @Override
    public Topic getTopicByCode(String namespace, String topic) {
        return TopicConverter.convert(topicRepository.getByCodeAndNamespace(topic, namespace));
    }

    @Override
    public PageResult<Topic> search(QPageQuery<TopicQuery> pageQuery) {
        return null;
    }

    @Override
    public PageResult<Topic> findUnsubscribedByQuery(QPageQuery<TopicQuery> pageQuery) {
        return null;
    }

    @Override
    public List<Topic> getAll() {
        return TopicConverter.convert(topicRepository.getAll());
    }

    @Override
    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {
        if (topicRepository.getByCodeAndNamespace(topic.getName().getCode(), topic.getName().getNamespace()) != null) {
            throw new NsrException(String.format("%s is already exist", topic.getName()));
        }

        TransactionContext.beginTransaction();

        try {
            topicRepository.add(TopicConverter.convert(topic));

            for (PartitionGroup partitionGroup : partitionGroups) {
                partitionGroupRepository.add(PartitionGroupConverter.convert(partitionGroup));

                for (Integer replica : partitionGroup.getReplicas()) {
                    // TODO id生成
                    partitionGroupReplicaRepository.add(new PartitionGroupReplicaDTO(
                            String.format("%s_%s", topic.getName().getFullName(), partitionGroup.getGroup()),
                            topic.getName().getCode(),
                            topic.getName().getNamespace(),
                            Long.valueOf(String.valueOf(replica)), partitionGroup.getGroup()));
                }
            }
            TransactionContext.commit();
        } catch (Exception e) {
            logger.error("add topic exception, topic: {}, groups: {}", topic, partitionGroups, e);
            TransactionContext.rollback();
            throw new NsrException(e);
        }
    }

    @Override
    public void removeTopic(Topic topic) {
        TopicDTO topicDTO = topicRepository.getByCodeAndNamespace(topic.getName().getCode(), topic.getName().getNamespace());
        if (topicDTO == null) {
            throw new NsrException(String.format("topic:%s is not exist", topic.getName()));
        }

        TransactionContext.beginTransaction();

        try {
            List<PartitionGroupDTO> partitionGroups = partitionGroupRepository.getByTopic(topicDTO.getCode(), topicDTO.getNamespace());
            List<PartitionGroupReplicaDTO> partitionGroupReplicas = partitionGroupReplicaRepository.getByTopic(topicDTO.getCode(), topicDTO.getNamespace());

            for (PartitionGroupDTO partitionGroup : partitionGroups) {
                partitionGroupRepository.delete(partitionGroup.getId());
            }

            for (PartitionGroupReplicaDTO partitionGroupReplica : partitionGroupReplicas) {
                partitionGroupReplicaRepository.deleteById(partitionGroupReplica.getId());
            }

            topicRepository.deleteById(topicDTO.getId());

            TransactionContext.commit();
        } catch (Exception e) {
            logger.error("remove topic exception, topic: {}", topic, e);
            TransactionContext.rollback();
            throw new NsrException(e);
        }
    }

    @Override
    public void addPartitionGroup(PartitionGroup group) {
        TopicDTO topicDTO = topicRepository.getByCodeAndNamespace(group.getTopic().getCode(), group.getTopic().getNamespace());
        if (topicDTO == null) {
            throw new NsrException(String.format("topic:%s is not exist", group.getTopic()));
        }

        TransactionContext.beginTransaction();

        try {
            if (PartitionGroup.ElectType.fix.equals(group.getElectType())) {
                group.setLeader(group.getReplicas().iterator().next());
            }

            partitionGroupRepository.add(PartitionGroupConverter.convert(group));

            for (Integer replica : group.getReplicas()) {
                // TODO id生成
                partitionGroupReplicaRepository.add(new PartitionGroupReplicaDTO(
                        String.format("%s_%s", group.getTopic().getFullName(), group.getGroup()),
                        group.getTopic().getCode(),
                        group.getTopic().getNamespace(),
                        Long.valueOf(String.valueOf(replica)), group.getGroup()));
            }

            topicDTO.setPartitions((short) (topicDTO.getPartitions() + group.getPartitions().size()));
            topicRepository.update(topicDTO);

            TransactionContext.commit();
        } catch (Exception e) {
            logger.error("addPartitionGroup exception, group: {}", group, e);
            TransactionContext.rollback();
            throw new NsrException(e);
        }
    }

    @Override
    public void removePartitionGroup(PartitionGroup group) {
        TopicDTO topicDTO = topicRepository.getByCodeAndNamespace(group.getTopic().getCode(), group.getTopic().getNamespace());
        if (topicDTO == null) {
            throw new NsrException(String.format("topic:%s is not exist", group.getTopic()));
        }

        TransactionContext.beginTransaction();

        try {
            List<PartitionGroupDTO> partitionGroups = partitionGroupRepository.getByTopic(topicDTO.getCode(), topicDTO.getNamespace());
            List<PartitionGroupReplicaDTO> partitionGroupReplicas = partitionGroupReplicaRepository.getByTopic(topicDTO.getCode(), topicDTO.getNamespace());

            for (PartitionGroupDTO partitionGroup : partitionGroups) {
                partitionGroupRepository.delete(partitionGroup.getId());
            }

            for (PartitionGroupReplicaDTO partitionGroupReplica : partitionGroupReplicas) {
                partitionGroupReplicaRepository.deleteById(partitionGroupReplica.getId());
            }

            topicDTO.setPartitions((short) (topicDTO.getPartitions() - group.getPartitions().size()));
            topicRepository.update(topicDTO);

            TransactionContext.commit();
        } catch (Exception e) {
            logger.error("removePartitionGroup exception, topic: {}", group.getTopic(), e);
            TransactionContext.rollback();
            throw new NsrException(e);
        }
    }

    @Override
    public Collection<Integer> updatePartitionGroup(PartitionGroup group) {
        return null;
    }

    @Override
    public void leaderChange(PartitionGroup group) {

    }

    @Override
    public List<PartitionGroup> getPartitionGroup(String namespace, String topic, Object[] groups) {
        List<PartitionGroup> result = Lists.newLinkedList();
        for (Object group : groups) {
            PartitionGroupDTO partitionGroupDTO = partitionGroupRepository.getByTopicAndGroup(topic, namespace, Integer.valueOf(String.valueOf(group)));
            if (partitionGroupDTO != null) {
                result.add(PartitionGroupConverter.convert(partitionGroupDTO));
            }
        }
        return result;
    }

    @Override
    public Topic update(Topic topic) {
        return TopicConverter.convert(topicRepository.update(TopicConverter.convert(topic)));
    }
}