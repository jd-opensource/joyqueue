package com.jd.journalq.manage;

import java.io.Serializable;

public class BrokerMetadata implements Serializable {
    private long brokerId;

    public long getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(long brokerId) {
        this.brokerId = brokerId;
    }
}