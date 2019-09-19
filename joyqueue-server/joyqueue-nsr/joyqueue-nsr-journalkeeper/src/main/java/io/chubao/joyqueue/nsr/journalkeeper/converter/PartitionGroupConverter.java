package io.chubao.joyqueue.nsr.journalkeeper.converter;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.journalkeeper.domain.PartitionGroupDTO;
import io.chubao.joyqueue.nsr.journalkeeper.helper.ArrayHelper;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * PartitionGroupConverter
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class PartitionGroupConverter {

    public static PartitionGroupDTO convert(PartitionGroup partitionGroup) {
        if (partitionGroup == null) {
            return null;
        }
        PartitionGroupDTO partitionGroupDTO = new PartitionGroupDTO();
        partitionGroupDTO.setId(generateId(partitionGroup));
        partitionGroupDTO.setTopic(partitionGroup.getTopic().getCode());
        partitionGroupDTO.setNamespace(partitionGroup.getTopic().getNamespace());
        partitionGroupDTO.setGroup(partitionGroup.getGroup());
        partitionGroupDTO.setPartitions(ArrayHelper.toString(partitionGroup.getPartitions()));
        partitionGroupDTO.setLeader(partitionGroup.getLeader());
        partitionGroupDTO.setRecLeader(partitionGroup.getRecLeader());
        partitionGroupDTO.setTerm(partitionGroup.getTerm());
        partitionGroupDTO.setReplicas(ArrayHelper.toString(partitionGroup.getReplicas()));
        partitionGroupDTO.setIsrs(ArrayHelper.toString(partitionGroup.getIsrs()));
        partitionGroupDTO.setLearners(ArrayHelper.toString(partitionGroup.getLearners()));
        partitionGroupDTO.setOutSyncReplicas(ArrayHelper.toString(partitionGroup.getOutSyncReplicas()));
        partitionGroupDTO.setElectType(Byte.valueOf((byte) partitionGroup.getElectType().type()));
        return partitionGroupDTO;
    }

    protected static String generateId(PartitionGroup partitionGroup) {
        return String.format("%s.%s", partitionGroup.getTopic().getFullName(), partitionGroup.getGroup());
    }

    public static PartitionGroup convert(PartitionGroupDTO partitionGroupDTO) {
        if (partitionGroupDTO == null) {
            return null;
        }
        PartitionGroup partitionGroup = new PartitionGroup();
        partitionGroup.setTopic(TopicName.parse(partitionGroupDTO.getTopic(), partitionGroupDTO.getNamespace()));
        partitionGroup.setGroup(partitionGroupDTO.getGroup());
        partitionGroup.setPartitions(ArrayHelper.toShortSet(partitionGroupDTO.getPartitions()));
        partitionGroup.setLeader(partitionGroupDTO.getLeader());
        partitionGroup.setRecLeader(partitionGroupDTO.getRecLeader());
        partitionGroup.setTerm(partitionGroupDTO.getTerm());
        partitionGroup.setReplicas(ArrayHelper.toIntSet(partitionGroupDTO.getReplicas()));
        partitionGroup.setIsrs(ArrayHelper.toIntSet(partitionGroupDTO.getIsrs()));
        partitionGroup.setLearners(ArrayHelper.toIntSet(partitionGroupDTO.getLearners()));
        partitionGroup.setOutSyncReplicas(ArrayHelper.toIntList(partitionGroupDTO.getOutSyncReplicas()));
        partitionGroup.setElectType(PartitionGroup.ElectType.valueOf(partitionGroupDTO.getElectType()));
        return partitionGroup;
    }

    public static List<PartitionGroup> convert(List<PartitionGroupDTO> partitionGroupDTOList) {
        if (CollectionUtils.isEmpty(partitionGroupDTOList)) {
            return Collections.emptyList();
        }
        List<PartitionGroup> result = Lists.newArrayListWithCapacity(partitionGroupDTOList.size());
        for (PartitionGroupDTO partitionGroupDTO : partitionGroupDTOList) {
            result.add(convert(partitionGroupDTO));
        }
        return result;
    }
}