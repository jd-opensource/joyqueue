package io.chubao.joyqueue.nsr.journalkeeper.domain;

import io.chubao.joyqueue.nsr.journalkeeper.helper.Column;

/**
 * ConsumerDTO
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class ConsumerDTO extends BaseDTO {

    private String id;
    private String namespace;
    private String topic;
    private String app;
    @Column(alias = "topic_type")
    private Byte topicType;
    @Column(alias = "client_type")
    private Byte clientType;
    private String referer;
    @Column(alias = "consume_policy")
    private String consumePolicy;
    @Column(alias = "retry_policy")
    private String retryPolicy;
    @Column(alias = "limit_policy")
    private String limitPolicy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public void setTopicType(Byte topicType) {
        this.topicType = topicType;
    }

    public Byte getTopicType() {
        return topicType;
    }

    public Byte getClientType() {
        return clientType;
    }

    public void setClientType(Byte clientType) {
        this.clientType = clientType;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getConsumePolicy() {
        return consumePolicy;
    }

    public void setConsumePolicy(String consumePolicy) {
        this.consumePolicy = consumePolicy;
    }

    public String getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(String retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public String getLimitPolicy() {
        return limitPolicy;
    }

    public void setLimitPolicy(String limitPolicy) {
        this.limitPolicy = limitPolicy;
    }
}