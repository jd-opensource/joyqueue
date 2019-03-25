package com.jd.journalq.common.domain;

import java.io.Serializable;

/**
 * @author wylixiaobin
 * Date: 2018/8/20
 */
public class Replica implements Serializable {
    private String id;
    /**
     * 主题
     */
    protected TopicName topic;
    /**
     * partition 分组
     */
    protected int group;
    /**
     * Broker ID
     */
    protected int brokerId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(int brokerId) {
        this.brokerId = brokerId;
    }

}
