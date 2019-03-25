package com.jd.journalq.network.command;

/**
 * FetchPartitionData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class FetchPartitionMessageData {

    private int count;
    private long index;

    public FetchPartitionMessageData() {

    }

    public FetchPartitionMessageData(int count, long index) {
        this.count = count;
        this.index = index;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getIndex() {
        return index;
    }
}