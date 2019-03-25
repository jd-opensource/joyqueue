package com.jd.journalq.service;

import com.jd.journalq.model.domain.PartitionGroupReplica;
import com.jd.journalq.model.domain.TopicPartitionGroup;
import com.jd.journalq.model.query.QPartitionGroupReplica;
import com.jd.journalq.nsr.NsrService;

/**
 * 主题Broker分组 服务
 * Created by chenyanying3 on 2018-10-18
 */
public interface PartitionGroupReplicaService extends NsrService<PartitionGroupReplica,QPartitionGroupReplica,String> {
    /**
     *扩容
     * @param replica
     * @param partitionGroup
     * @return
     */
    public int addWithNameservice(PartitionGroupReplica replica, TopicPartitionGroup partitionGroup);

    /**
     * 锁容
     * @param replica
     * @param partitionGroup
     * @return
     */
    public int removeWithNameservice(PartitionGroupReplica replica, TopicPartitionGroup partitionGroup);
}
