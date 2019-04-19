package com.jd.journalq.broker.kafka.coordinator.transaction.domain;

/**
 * UnCompletedTransaction
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/19
 */
public class UnCompletedTransaction extends TransactionMetadata {

    private long startIndex;
    private long endIndex;
    private int reties = 0;
    private boolean expire = false;

    public void setStartIndex(long startIndex) {
        this.startIndex = startIndex;
    }

    public long getStartIndex() {
        return startIndex;
    }

    public void setEndIndex(long endIndex) {
        this.endIndex = endIndex;
    }

    public long getEndIndex() {
        return endIndex;
    }

    public void incrReties() {
        reties ++;
    }

    public void setReties(int reties) {
        this.reties = reties;
    }

    public int getReties() {
        return reties;
    }

    public void setExpire(boolean expire) {
        this.expire = expire;
    }

    @Override
    public boolean isExpired() {
        return super.isExpired();
    }
}