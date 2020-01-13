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

    @Override
    public String toString() {
        return "Subscribe{" +
                "topic=" + topic +
                ", app=" + app +
                ", type=" + type +
                ", clientType=" + clientType +
                ", namespace=" + namespace +
                ", subscribeGroup='" + subscribeGroup + '\'' +
                '}';
    }
}
