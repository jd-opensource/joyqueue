package io.chubao.joyqueue.model.domain;

import io.chubao.joyqueue.model.domain.nsr.BaseNsrModel;

public class Consumer extends BaseNsrModel {

    public static final int CONSUMER_TYPE = 2;

    private Identity app;

    private Topic topic;

    private Identity owner;

    private Namespace namespace;

    /**
     * 订阅分组
     */
    private String subscribeGroup;

    /**
     * 客户端类型
     **/
    private byte clientType;

    /**
     * 类型 0:topic,1:sequential,2:broadcast,
     */
    private byte topicType;

    /**
     *  消费配置, null indicate not default config
     */
    private ConsumerConfig config;

    public Identity getApp() {
        return app;
    }

    public void setApp(Identity app) {
        this.app = app;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Identity getOwner() {
        return owner;
    }

    public void setOwner(Identity owner) {
        this.owner = owner;
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

    public byte getTopicType() {
        return topicType;
    }

    public void setTopicType(byte topicType) {
        this.topicType = topicType;
    }

    public ConsumerConfig getConfig() {
        return config;
    }

    public void setConfig(ConsumerConfig config) {
        this.config = config;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Consumer consumer = (Consumer) o;
        if (id != null) {
            if (id.equals(consumer.getId()) ) {
                return true;
            }
        }
        if (topic != null) {
            if (!topic.equals(consumer.topic)) return false;
        }
        if (app != null) {
            return app.equals(consumer.app);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
