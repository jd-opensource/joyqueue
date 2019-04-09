package com.jd.journalq.model.query;

import com.jd.jmq.common.model.QKeyword;
import com.jd.jmq.common.model.Query;
import com.jd.jmq.model.domain.Identity;

/**
 * 主题
 * Created by chenyanying3 on 2018-10-17
 */
public class QTopic extends QKeyword implements Query {
    protected int type = -1;

    /**
     * 订阅类型：1：生产者， 2：消费者
     */
    public Integer subscribeType;

    public String namespace;

    public String code;

    public Identity app;

    public QTopic() {
    }

    public QTopic(String namespace, String code) {
        this.namespace = namespace;
        this.code = code;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Integer getSubscribeType() {
        return subscribeType;
    }

    public void setSubscribeType(Integer subscribeType) {
        this.subscribeType = subscribeType;
    }

    public Identity getApp() {
        return app;
    }

    public void setApp(Identity app) {
        this.app = app;
    }

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
}
