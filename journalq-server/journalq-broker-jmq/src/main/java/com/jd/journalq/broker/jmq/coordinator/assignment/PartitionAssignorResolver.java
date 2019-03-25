package com.jd.journalq.broker.jmq.coordinator.assignment;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.jmq.config.JMQConfig;
import com.jd.journalq.broker.jmq.coordinator.domain.JMQCoordinatorGroup;
import com.jd.journalq.broker.jmq.coordinator.domain.JMQCoordinatorGroupMember;
import com.jd.journalq.broker.jmq.coordinator.domain.PartitionAssignment;
import com.jd.journalq.broker.jmq.exception.JMQException;
import com.jd.journalq.common.domain.PartitionGroup;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import com.jd.laf.extension.SpiLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * PartitionAssignor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class PartitionAssignorResolver {

    protected static final Logger logger = LoggerFactory.getLogger(PartitionAssignorResolver.class);

    private JMQConfig config;
    private ExtensionPoint<PartitionAssignor, String> partitionAssignors = new ExtensionPointLazy<>(PartitionAssignor.class, SpiLoader.INSTANCE, null, null);

    public PartitionAssignorResolver(JMQConfig config) {
        this.config = config;
    }

    public PartitionAssignment assign(JMQCoordinatorGroup group, JMQCoordinatorGroupMember member, String topic, List<PartitionGroup> partitionGroups) {
        String assignType = config.getCoordinatorPartitionAssignType();
        PartitionAssignor partitionAssignor = (StringUtils.isBlank(assignType) ? null : partitionAssignors.get(assignType));

        if (partitionAssignor == null) {
            throw new JMQException(JMQCode.FW_COORDINATOR_PARTITION_ASSIGNOR_TYPE_NOT_EXIST.getMessage(assignType),
                    JMQCode.FW_COORDINATOR_PARTITION_ASSIGNOR_TYPE_NOT_EXIST.getCode());
        }

        if (!StringUtils.equals(group.getAssignType(), assignType)) {
            group.setAssignContext(Maps.newHashMap());
            group.setAssignType(assignType);
            member.setTimeoutCallback(null);
        }

        return partitionAssignor.assign(group, member, topic, partitionGroups);
    }
}