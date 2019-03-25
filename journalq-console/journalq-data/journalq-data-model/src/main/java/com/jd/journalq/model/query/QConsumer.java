package com.jd.journalq.model.query;

import com.jd.journalq.common.model.QKeyword;
import com.jd.journalq.model.domain.Identity;
import com.jd.journalq.model.domain.Namespace;
import com.jd.journalq.model.domain.Topic;

import java.util.List;

public class QConsumer extends QKeyword implements QIdentity {
    private Topic topic;
    private Identity app;
    private byte clientType = -1;
    private String namespace;
    private List<String> appList;
    private String referer;

    public QConsumer() {}

    public QConsumer(String topicCode, String namespaceCode, String appCode) {
        this.topic = new Topic(topicCode);
        this.topic.setNamespace(new Namespace(namespaceCode));
        this.app = new Identity(appCode);
        this.namespace = namespaceCode;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Identity getApp() {
        return app;
    }

    public void setApp(Identity app) {
        this.app = app;
    }

    public byte getClientType() {
        return clientType;
    }

    public void setClientType(byte clientType) {
        this.clientType = clientType;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<String> getAppList() {
        return appList;
    }

    public void setAppList(List<String> appList) {
        this.appList = appList;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }
}