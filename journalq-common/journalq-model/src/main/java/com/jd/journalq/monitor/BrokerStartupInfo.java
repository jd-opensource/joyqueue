package com.jd.journalq.monitor;

import java.io.Serializable;

/**
 * Created by wangxiaofei1 on 2019/5/23.
 */
public class BrokerStartupInfo implements Serializable {
    private long startupTime;
    private String revision;


    public long getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(long startupTime) {
        this.startupTime = startupTime;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }
}
