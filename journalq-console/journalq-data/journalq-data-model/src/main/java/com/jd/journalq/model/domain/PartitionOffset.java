package com.jd.journalq.model.domain;

public class PartitionOffset {
    private  short partition;
    private  long  offset;

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

   public enum Location{
        MIN,MAX
    }
}
