package io.chubao.joyqueue.monitor;

/**
 * 积压信息
 *
 * author: gaohaoxiang
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