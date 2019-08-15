package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.domain.PartitionGroupReplica;
import io.chubao.joyqueue.model.domain.TopicPartitionGroup;
import io.chubao.joyqueue.model.query.QPartitionGroupReplica;
import io.chubao.joyqueue.nsr.NsrService;

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
    int addWithNameservice(PartitionGroupReplica replica, TopicPartitionGroup partitionGroup);

    /**
     * 锁容
     * @param replica
     * @param partitionGroup
     * @return
     */
    int removeWithNameservice(PartitionGroupReplica replica, TopicPartitionGroup partitionGroup);
}
