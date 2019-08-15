package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.domain.Namespace;
import io.chubao.joyqueue.model.domain.Topic;
import io.chubao.joyqueue.model.domain.TopicPartitionGroup;
import io.chubao.joyqueue.model.query.QTopicPartitionGroup;
import io.chubao.joyqueue.nsr.NsrService;

import java.util.List;

/**
 * Topic partition group service
 * Created by chenyanying3 on 2018-10-18
 */
public interface TopicPartitionGroupService extends NsrService<TopicPartitionGroup, QTopicPartitionGroup,String> {

   TopicPartitionGroup findByTopicAndGroup(String namespace, String topic,  Integer groupNo);

   List<TopicPartitionGroup> findByTopic(Namespace namespace, Topic topic);
   int removePartition(TopicPartitionGroup model) throws Exception;
   int addPartition(TopicPartitionGroup model) throws Exception;

   int leaderChange(TopicPartitionGroup model) throws Exception;

}
