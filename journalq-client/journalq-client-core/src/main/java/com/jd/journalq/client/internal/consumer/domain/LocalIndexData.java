package com.jd.journalq.client.internal.consumer.domain;

import com.jd.journalq.toolkit.time.SystemClock;

/**
 * LocalIndexData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/14
 */
public class LocalIndexData {

    private long index;
    private long updateTime;
    private long createTime;

    public LocalIndexData() {

    }

    public LocalIndexData(long index, long updateTime, long createTime) {
        this.index = index;
        this.updateTime = updateTime;
        this.createTime = createTime;
    }

    public boolean isExpired(long expireTime) {
        return (SystemClock.now() - updateTime > expireTime);
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}