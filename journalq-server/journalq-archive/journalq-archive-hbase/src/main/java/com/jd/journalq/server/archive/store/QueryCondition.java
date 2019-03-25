package com.jd.journalq.server.archive.store;

import com.jd.journalq.server.archive.store.model.Query;

/**
 * Created by chengzhiliang on 2018/12/4.
 */
public class QueryCondition implements Query {

    @Override
    public <T> T getQueryCondition() {
        return (T)this;
    }

    private RowKey startRowKey; // 查询开始键
    private RowKey stopRowKey; // 查询结束键
    private int count;

    private RowKey rowKey; // 指定RowKey查询

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public RowKey getStartRowKey() {
        return startRowKey;
    }

    public void setStartRowKey(RowKey startRowKey) {
        this.startRowKey = startRowKey;
    }

    public RowKey getStopRowKey() {
        return stopRowKey;
    }

    public void setStopRowKey(RowKey stopRowKey) {
        this.stopRowKey = stopRowKey;
    }

    public RowKey getRowKey() {
        return rowKey;
    }

    public void setRowKey(RowKey rowKey) {
        this.rowKey = rowKey;
    }

    /**
     * 查询RowKey
     */
    public static class RowKey {
        private String topic;
        private long time;
        private String businessId;
        private String messageId;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

    }
}
