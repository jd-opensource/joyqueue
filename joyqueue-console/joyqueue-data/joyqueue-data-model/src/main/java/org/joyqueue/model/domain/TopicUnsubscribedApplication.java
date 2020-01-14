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
public class TopicUnsubscribedApplication extends Application {

    private String topicCode;
    private Boolean subscribeGroupExist;
    private int subscribeType;

    public TopicUnsubscribedApplication(Application app) {
        this.setCode(app.getCode());
        this.setId(app.getId());
        this.setAliasCode(app.getAliasCode());
        this.setSystem(app.getSystem());
        this.setOwner(app.getOwner());
    }

    public String getTopicCode() {
        return topicCode;
    }

    public void setTopicCode(String topicCode) {
        this.topicCode = topicCode;
    }

    public Boolean isSubscribeGroupExist() {
        return subscribeGroupExist;
    }

    public void setSubscribeGroupExist(Boolean subscribeGroupExist) {
        this.subscribeGroupExist = subscribeGroupExist;
    }

    public int getSubscribeType() {
        return subscribeType;
    }

    public void setSubscribeType(int subscribeType) {
        this.subscribeType = subscribeType;
    }
}