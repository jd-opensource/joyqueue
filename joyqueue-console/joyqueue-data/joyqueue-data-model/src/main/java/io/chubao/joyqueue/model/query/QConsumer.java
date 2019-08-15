package io.chubao.joyqueue.model.query;

import io.chubao.joyqueue.model.QKeyword;
import io.chubao.joyqueue.model.domain.Identity;
import io.chubao.joyqueue.model.domain.Namespace;
import io.chubao.joyqueue.model.domain.Topic;

import java.util.List;

public class QConsumer extends QKeyword implements QIdentity {
    private Topic topic;
    private Identity app;
    private byte clientType = -1;
    private String namespace;
    private List<String> appList;
    private String referer;

    public QConsumer() {}

    public QConsumer(Topic topic) {
        this.topic = topic;
        this.namespace = topic.getNamespace().getCode();
    }

    public QConsumer(String referer) {
        this.referer = referer;
    }

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