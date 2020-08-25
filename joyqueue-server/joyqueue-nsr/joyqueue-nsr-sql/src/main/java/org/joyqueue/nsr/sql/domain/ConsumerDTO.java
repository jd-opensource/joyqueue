/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.nsr.sql.domain;

import org.joyqueue.nsr.sql.helper.Column;

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
    private String group;
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

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
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