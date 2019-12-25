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
import org.joyqueue.model.Query;
import org.joyqueue.model.domain.Identity;

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
