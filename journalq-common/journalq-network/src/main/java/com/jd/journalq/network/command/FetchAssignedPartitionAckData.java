package com.jd.journalq.network.command;

import com.jd.journalq.exception.JMQCode;

import java.util.Collections;
import java.util.List;

/**
 * FetchAssignedPartitionAckData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/17
 */
public class FetchAssignedPartitionAckData {

    private List<Short> partitions;
    private JMQCode code;

    public FetchAssignedPartitionAckData() {

    }

    public FetchAssignedPartitionAckData(JMQCode code) {
        this.partitions = Collections.emptyList();
        this.code = code;
    }

    public FetchAssignedPartitionAckData(List<Short> partitions, JMQCode code) {
        this.partitions = partitions;
        this.code = code;
    }

    public void setPartitions(List<Short> partitions) {
        this.partitions = partitions;
    }

    public List<Short> getPartitions() {
        return partitions;
    }

    public void setCode(JMQCode code) {
        this.code = code;
    }

    public JMQCode getCode() {
        return code;
    }
}