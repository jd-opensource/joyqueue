package io.chubao.joyqueue.nsr.service;


import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.model.ReplicaQuery;

import java.util.List;


/**
 * @author lixiaobin6
 * 下午3:11 2018/8/13
 */
public interface PartitionGroupReplicaService extends DataService<Replica, ReplicaQuery, String> {
    /**
     * 根据Topic删除
     *
     * @param topic
     */
    void deleteByTopic(TopicName topic);

    /**
     * 根据partitionGroup删除
     *
     * @param topic
     * @param groupNo
     */
    void deleteByTopicAndPartitionGroup(TopicName topic, int groupNo);

    /**
     * 根据Topic查找
     ** @param topic
     * @return
     */
    List<Replica> findByTopic(TopicName topic);

    /**
     * 根据Topic和PartitionGroup查找
     *
\     * @param topic
     * @param groupNo
     * @return
     */
    List<Replica> findByTopicAndGrPartitionGroup(TopicName topic, int groupNo);

    /**
     * 根据BrokerId查找
     *
     * @param brokerId
     * @return
     */
    List<Replica> findByBrokerId(Integer brokerId);
}
