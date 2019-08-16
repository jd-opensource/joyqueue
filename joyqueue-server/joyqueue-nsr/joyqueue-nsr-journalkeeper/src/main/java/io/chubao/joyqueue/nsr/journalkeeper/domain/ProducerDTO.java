package io.chubao.joyqueue.nsr.journalkeeper.domain;

import io.chubao.joyqueue.nsr.journalkeeper.helper.Column;

/**
 * ProducerDTO
 * author: gaohaoxiang
 * date: 2019/8/16
 */
public class ProducerDTO extends BaseDTO {

    private String id;
    private String namespace;
    private String topic;
    private String app;
    @Column(alias = "client_type")
    private Byte clientType;
    @Column(alias = "produce_policy")
    private String producePolicy;
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

    public Byte getClientType() {
        return clientType;
    }

    public void setClientType(Byte clientType) {
        this.clientType = clientType;
    }

    public void setProducePolicy(String producePolicy) {
        this.producePolicy = producePolicy;
    }

    public String getProducePolicy() {
        return producePolicy;
    }

    public String getLimitPolicy() {
        return limitPolicy;
    }

    public void setLimitPolicy(String limitPolicy) {
        this.limitPolicy = limitPolicy;
    }
}