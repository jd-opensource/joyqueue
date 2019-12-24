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
package org.joyqueue.model.domain;

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