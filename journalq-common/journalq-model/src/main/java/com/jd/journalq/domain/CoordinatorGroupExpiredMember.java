package com.jd.journalq.domain;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * CoordinatorGroupExpiredMember
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class CoordinatorGroupExpiredMember {

    private String host;
    private AtomicInteger expireTimes = new AtomicInteger(0);
    private long latestHeartbeat;
    private long expireTime;

    public CoordinatorGroupExpiredMember() {

    }

    public CoordinatorGroupExpiredMember(String host) {
        this.host = host;
    }


    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setExpireTimes(AtomicInteger expireTimes) {
        this.expireTimes = expireTimes;
    }

    public AtomicInteger getExpireTimes() {
        return expireTimes;
    }

    public long getLatestHeartbeat() {
        return latestHeartbeat;
    }

    public void setLatestHeartbeat(long latestHeartbeat) {
        this.latestHeartbeat = latestHeartbeat;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public long getExpireTime() {
        return expireTime;
    }
}