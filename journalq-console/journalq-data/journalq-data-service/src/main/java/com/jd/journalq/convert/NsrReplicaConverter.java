package com.jd.journalq.convert;

import com.jd.journalq.common.domain.Replica;
import com.jd.journalq.common.domain.TopicName;
import com.jd.journalq.model.domain.Namespace;
import com.jd.journalq.model.domain.PartitionGroupReplica;
import com.jd.journalq.model.domain.Topic;

/**
 * Created by wangxiaofei1 on 2019/1/3.
 */
public class NsrReplicaConverter extends Converter<PartitionGroupReplica,Replica> {
    @Override
    protected Replica forward(PartitionGroupReplica partitionGroupReplica) {
        Replica replica = new Replica();
        replica.setId(partitionGroupReplica.getId());
        replica.setBrokerId(partitionGroupReplica.getBrokerId());
        replica.setGroup(partitionGroupReplica.getGroupNo());
        replica.setTopic(new TopicName(partitionGroupReplica.getTopic().getCode(),partitionGroupReplica.getNamespace().getCode()));
        return replica;
    }

    @Override
    protected PartitionGroupReplica backward(Replica replica) {
        PartitionGroupReplica  partitionGroupReplica = new PartitionGroupReplica();
        partitionGroupReplica.setId(replica.getId());
        partitionGroupReplica.setBrokerId(replica.getBrokerId());
        partitionGroupReplica.setGroupNo(replica.getGroup());
        partitionGroupReplica.setNamespace(new Namespace(replica.getTopic().getNamespace()));
        partitionGroupReplica.setTopic(new Topic(replica.getTopic().getCode()));
        return partitionGroupReplica;
    }
}
