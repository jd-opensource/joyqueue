package com.jd.journalq.service;

import com.jd.journalq.model.domain.Namespace;
import com.jd.journalq.model.domain.Topic;
import com.jd.journalq.model.domain.TopicPartitionGroup;
import com.jd.journalq.model.query.QTopicPartitionGroup;
import com.jd.journalq.nsr.NsrService;

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
