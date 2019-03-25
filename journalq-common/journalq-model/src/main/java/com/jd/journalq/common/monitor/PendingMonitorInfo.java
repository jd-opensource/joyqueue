package com.jd.journalq.common.monitor;

/**
 * 积压信息
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/11
 */
public class PendingMonitorInfo extends BaseMonitorInfo {

    private long count;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}