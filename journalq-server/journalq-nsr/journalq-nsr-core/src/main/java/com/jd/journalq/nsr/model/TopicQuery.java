package com.jd.journalq.nsr.model;

import com.jd.journalq.model.Query;

public class TopicQuery implements Query {
    private String namespace;
    private String code;
    private Byte type;
    /**
     * 订阅类型：1：生产者， 2：消费者
     */
    public Integer subscribeType;

    public String app;

    public String keyword;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public Integer getSubscribeType() {
        return subscribeType;
    }

    public void setSubscribeType(Integer subscribeType) {
        this.subscribeType = subscribeType;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
