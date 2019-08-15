package io.chubao.joyqueue.nsr.service;


import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.model.PartitionGroupQuery;

import java.util.List;

/**
 * @author lixiaobin6
 * 下午3:11 2018/8/13
 */
public interface PartitionGroupService extends DataService<PartitionGroup, PartitionGroupQuery, String> {
    /**
     * 根据Topic和PartitionGroup查找
     * * @param namespace
     *
     * @param topic
     * @param group
     * @return
     */
    PartitionGroup findByTopicAndGroup(TopicName topic, int group);

    /**
     * 根据Topic查找
     *
     * @param topic
     * @return
     */
    List<PartitionGroup> getByTopic(TopicName topic);
}
