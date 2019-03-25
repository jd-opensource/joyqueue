package com.jd.journalq.server.retry.remote.command;

import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * 获取重试
 */
public class GetRetry extends JMQPayload {

    // 主题
    private String topic;
    // 应用
    private String app;
    // 数量
    private short count;
    // 起始ID
    private long startId;

    @Override
    public int type() {
        return CommandType.GET_RETRY;
    }

    public GetRetry topic(final String topic) {
        setTopic(topic);
        return this;
    }

    public GetRetry app(final String app) {
        setApp(app);
        return this;
    }

    public GetRetry count(final short count) {
        setCount(count);
        return this;
    }

    public GetRetry startId(final long startId) {
        setStartId(startId);
        return this;
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getApp() {
        return this.app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public short getCount() {
        return this.count;
    }

    public void setCount(short count) {
        this.count = count;
    }

    public long getStartId() {
        return startId;
    }

    public void setStartId(long startId) {
        this.startId = startId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GetRetry{");
        sb.append("topic='").append(topic).append('\'');
        sb.append(", app='").append(app).append('\'');
        sb.append(", count=").append(count);
        sb.append(", startId=").append(startId);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        GetRetry getRetry = (GetRetry) o;

        if (count != getRetry.count) {
            return false;
        }
        if (startId != getRetry.startId) {
            return false;
        }
        if (app != null ? !app.equals(getRetry.app) : getRetry.app != null) {
            return false;
        }
        if (topic != null ? !topic.equals(getRetry.topic) : getRetry.topic != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (topic != null ? topic.hashCode() : 0);
        result = 31 * result + (app != null ? app.hashCode() : 0);
        result = 31 * result + (int) count;
        result = 31 * result + (int) (startId ^ (startId >>> 32));
        return result;
    }


}