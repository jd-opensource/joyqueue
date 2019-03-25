package com.jd.journalq.monitor;

/**
 * @author majun8
 */
public class MqttSummaryInfo extends BaseMonitorInfo {

    private long totalConnections;
    private long totalSessions;
    private long totalPublished;
    private long totalConsumed;
    private long totalAcknowledged;
    private long totalRecommit;
    private int consumePool;
    private int deliveryPool;

    public long getTotalConnections() {
        return totalConnections;
    }

    public void setTotalConnections(long totalConnections) {
        this.totalConnections = totalConnections;
    }

    public long getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(long totalSessions) {
        this.totalSessions = totalSessions;
    }

    public long getTotalPublished() {
        return totalPublished;
    }

    public void setTotalPublished(long totalPublished) {
        this.totalPublished = totalPublished;
    }

    public long getTotalConsumed() {
        return totalConsumed;
    }

    public void setTotalConsumed(long totalConsumed) {
        this.totalConsumed = totalConsumed;
    }

    public long getTotalAcknowledged() {
        return totalAcknowledged;
    }

    public void setTotalAcknowledged(long totalAcknowledged) {
        this.totalAcknowledged = totalAcknowledged;
    }

    public long getTotalRecommit() {
        return totalRecommit;
    }

    public void setTotalRecommit(long totalRecommit) {
        this.totalRecommit = totalRecommit;
    }

    public int getConsumePool() {
        return consumePool;
    }

    public void setConsumePool(int consumePool) {
        this.consumePool = consumePool;
    }

    public int getDeliveryPool() {
        return deliveryPool;
    }

    public void setDeliveryPool(int deliveryPool) {
        this.deliveryPool = deliveryPool;
    }
}
