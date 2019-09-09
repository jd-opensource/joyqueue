package io.chubao.joyqueue.nsr.journalkeeper.converter;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.journalkeeper.domain.PartitionGroupReplicaDTO;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * PartitionGroupReplicaConverter
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class PartitionGroupReplicaConverter {

    public static String generateId(Replica replica) {
        return generateId(replica.getTopic().getFullName(), replica.getGroup(), replica.getBrokerId());
    }

    public static String generateId(String topic, int group, int replica) {
        return String.format("%s.%s.%s", topic, group, replica);
    }

    public static PartitionGroupReplicaDTO convert(Replica replica) {
        if (replica == null) {
            return null;
        }
        PartitionGroupReplicaDTO partitionGroupReplicaDTO = new PartitionGroupReplicaDTO();
        partitionGroupReplicaDTO.setId(generateId(replica));
        partitionGroupReplicaDTO.setTopic(replica.getTopic().getCode());
        partitionGroupReplicaDTO.setNamespace(replica.getTopic().getNamespace());
        partitionGroupReplicaDTO.setGroup(replica.getGroup());
        partitionGroupReplicaDTO.setBrokerId(Long.valueOf(replica.getBrokerId()));
        return partitionGroupReplicaDTO;
    }

    public static Replica convert(PartitionGroupReplicaDTO partitionGroupReplicaDTO) {
        if (partitionGroupReplicaDTO == null) {
            return null;
        }
        Replica replica = new Replica();
        replica.setId(partitionGroupReplicaDTO.getId());
        replica.setTopic(TopicName.parse(partitionGroupReplicaDTO.getTopic(), partitionGroupReplicaDTO.getNamespace()));
        replica.setBrokerId(Integer.valueOf(String.valueOf(partitionGroupReplicaDTO.getBrokerId())));
        replica.setGroup(partitionGroupReplicaDTO.getGroup());
        return replica;
    }

    public static List<Replica> convert(List<PartitionGroupReplicaDTO> partitionGroupReplicaDTOS) {
        if (CollectionUtils.isEmpty(partitionGroupReplicaDTOS)) {
            return Collections.emptyList();
        }
        List<Replica> result = Lists.newArrayListWithCapacity(partitionGroupReplicaDTOS.size());
        for (PartitionGroupReplicaDTO partitionGroupReplicaDTO : partitionGroupReplicaDTOS) {
            result.add(convert(partitionGroupReplicaDTO));
        }
        return result;
    }
}