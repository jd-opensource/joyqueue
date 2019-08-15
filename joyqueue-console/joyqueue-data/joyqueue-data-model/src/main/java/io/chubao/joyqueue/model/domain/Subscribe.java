package io.chubao.joyqueue.model.domain;

import java.io.Serializable;

public class Subscribe implements Serializable {
    private Topic topic;
    private Identity app;
    private SubscribeType type; // producer or consumer
    private byte clientType; //include joyqueue,kafka et.
    private Namespace namespace;
    private String subscribeGroup;

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

    public SubscribeType getType() {
        return type;
    }

    public void setType(Object typeValue) {
        this.type = SubscribeType.resolve(typeValue);
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public String getSubscribeGroup() {
        return subscribeGroup;
    }

    public void setSubscribeGroup(String subscribeGroup) {
        this.subscribeGroup = subscribeGroup;
    }

    public byte getClientType() {
        return clientType;
    }

    public void setClientType(byte clientType) {
        this.clientType = clientType;
    }
}
