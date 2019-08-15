package io.chubao.joyqueue.nsr;

import io.chubao.joyqueue.model.domain.TopicPartitionGroup;
import io.chubao.joyqueue.model.query.QTopicPartitionGroup;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/1/3.
 */
public interface PartitionGroupServerService extends NsrService<TopicPartitionGroup,QTopicPartitionGroup,String> {
    List<TopicPartitionGroup> findByTopic(String topic);
    List<TopicPartitionGroup> findByTopic(String topic,String namespace);
    TopicPartitionGroup findByTopicAndGroup(String namespace, String topic, Integer groupNo);
}
