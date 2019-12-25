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
package org.joyqueue.model.query;

import org.joyqueue.model.QKeyword;
import org.joyqueue.model.domain.Topic;

import java.util.List;

/**
 * Created by yangyang115 on 18-7-26.
 */
public class QApplication extends QKeyword {

    /**
     * 订阅类型：1：生产者， 2：消费者
     */
    public Integer subscribeType;

    public Topic topic;

    //不包含这些应用
    public List<String> noInCodes;

    public List<String> appList;

    public QApplication() {

    }

    public QApplication(String keyword, Long userId) {
        super(keyword);
        this.userId = userId;
    }

    public QApplication(Long userId) {
        this.userId = userId;
    }

    public Integer getSubscribeType() {
        return subscribeType;
    }

    public void setSubscribeType(Integer subscribeType) {
        this.subscribeType = subscribeType;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public List<String> getNoInCodes() {
        return noInCodes;
    }

    public void setNoInCodes(List<String> noInCodes) {
        this.noInCodes = noInCodes;
    }

    public List<String> getAppList() {
        return appList;
    }

    public void setAppList(List<String> appList) {
        this.appList = appList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QApplication{");
        sb.append("userId=").append(userId);
        sb.append(", keyword='").append(keyword).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
