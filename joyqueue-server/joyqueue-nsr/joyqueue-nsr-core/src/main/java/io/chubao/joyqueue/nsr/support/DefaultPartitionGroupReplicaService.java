package io.chubao.joyqueue.nsr.support;

import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.service.PartitionGroupReplicaService;
import io.chubao.joyqueue.nsr.service.internal.PartitionGroupReplicaInternalService;

import java.util.List;

/**
 * DefaultPartitionGroupReplicaService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class DefaultPartitionGroupReplicaService implements PartitionGroupReplicaService {

    private PartitionGroupReplicaInternalService partitionGroupReplicaInternalService;

    public DefaultPartitionGroupReplicaService(PartitionGroupReplicaInternalService partitionGroupReplicaInternalService) {
        this.partitionGroupReplicaInternalService = partitionGroupReplicaInternalService;
    }

    @Override
    public Replica getById(String id) {
        return partitionGroupReplicaInternalService.getById(id);
    }

    @Override
    public List<Replica> getByTopic(TopicName topic) {
        return partitionGroupReplicaInternalService.getByTopic(topic);
    }

    @Override
    public List<Replica> getByTopicAndGroup(TopicName topic, int groupNo) {
        return partitionGroupReplicaInternalService.getByTopicAndGroup(topic, groupNo);
    }

    @Override
    public List<Replica> getByBrokerId(Integer brokerId) {
        return partitionGroupReplicaInternalService.getByBrokerId(brokerId);
    }

    @Override
    public List<Replica> getAll() {
        return partitionGroupReplicaInternalService.getAll();
    }

    @Override
    public Replica add(Replica replica) {
        return partitionGroupReplicaInternalService.add(replica);
    }

    @Override
    public Replica update(Replica replica) {
        return partitionGroupReplicaInternalService.update(replica);
    }

    @Override
    public void delete(String id) {
        partitionGroupReplicaInternalService.delete(id);
    }
}
