package io.chubao.joyqueue.nsr;

import io.chubao.joyqueue.model.domain.PartitionGroupReplica;
import io.chubao.joyqueue.model.query.QPartitionGroupReplica;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/1/3.
 */
public interface ReplicaServerService extends NsrService<PartitionGroupReplica,QPartitionGroupReplica,String> {
    int deleteByGroup(String topic, int groupNo);
    List<PartitionGroupReplica> findByTopic(String topic);
    
    int deleteByTopic(String topic);
}
