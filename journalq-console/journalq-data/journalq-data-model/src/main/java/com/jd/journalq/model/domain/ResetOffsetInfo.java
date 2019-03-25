package com.jd.journalq.model.domain;

import java.util.List;

public class ResetOffsetInfo {
    private Subscribe subscribe;
    private List<PartitionOffset> partitionOffsets;

    public Subscribe getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(Subscribe subscribe) {
        this.subscribe = subscribe;
    }

    public List<PartitionOffset> getPartitionOffsets() {
        return partitionOffsets;
    }

    public void setPartitionOffsets(List<PartitionOffset> partitionOffsets) {
        this.partitionOffsets = partitionOffsets;
    }
}
