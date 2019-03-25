package com.jd.journalq.model.domain;

/**
 * 某个App未订阅的主题
 * Created by chenyanying3 on 2018-10-17
 */
public class AppUnsubscribedTopic extends Topic {

    private String appCode;
    private int subscribeType;
    private Boolean subscribeGroupExist;

    public AppUnsubscribedTopic(Topic topic) {
        this.setId(topic.getId());
        this.setCode(topic.getCode());
        this.setNamespace(topic.getNamespace());
        this.setType(topic.getType());
        this.setBrokers(topic.getBrokers());
        this.setElectType(topic.getElectType());
        this.setPartitions(topic.getPartitions());
        this.setBrokerGroup(topic.getBrokerGroup());
        this.setReplica(topic.getReplica());
    }

    public int getSubscribeType() {
        return subscribeType;
    }

    public void setSubscribeType(int subscribeType) {
        this.subscribeType = subscribeType;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public Boolean isSubscribeGroupExist() {
        return subscribeGroupExist;
    }

    public void setSubscribeGroupExist(Boolean subscribeGroupExist) {
        this.subscribeGroupExist = subscribeGroupExist;
    }
}