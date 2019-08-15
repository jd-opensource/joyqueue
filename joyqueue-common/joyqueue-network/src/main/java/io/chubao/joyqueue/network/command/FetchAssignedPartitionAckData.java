package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.exception.JoyQueueCode;

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
    private JoyQueueCode code;

    public FetchAssignedPartitionAckData() {

    }

    public FetchAssignedPartitionAckData(JoyQueueCode code) {
        this.partitions = Collections.emptyList();
        this.code = code;
    }

    public FetchAssignedPartitionAckData(List<Short> partitions, JoyQueueCode code) {
        this.partitions = partitions;
        this.code = code;
    }

    public void setPartitions(List<Short> partitions) {
        this.partitions = partitions;
    }

    public List<Short> getPartitions() {
        return partitions;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }

    public JoyQueueCode getCode() {
        return code;
    }
}