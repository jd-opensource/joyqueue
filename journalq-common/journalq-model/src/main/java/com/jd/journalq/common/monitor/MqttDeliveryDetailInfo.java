package com.jd.journalq.common.monitor;

/**
 * @author majun8
 */
public class MqttDeliveryDetailInfo extends BaseMonitorInfo {
    private int threadId;
    private int totalClients;

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public int getTotalClients() {
        return totalClients;
    }

    public void setTotalClients(int totalClients) {
        this.totalClients = totalClients;
    }
}
