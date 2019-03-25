package com.jd.journalq.nsr;

import com.jd.journalq.model.domain.PartitionGroupReplica;
import com.jd.journalq.model.query.QPartitionGroupReplica;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/1/3.
 */
public interface ReplicaServerService extends NsrService<PartitionGroupReplica,QPartitionGroupReplica,String> {
    int deleteByGroup(String topic, int groupNo);
    List<PartitionGroupReplica> findByTopic(String topic);
    
    int deleteByTopic(String topic);
}
