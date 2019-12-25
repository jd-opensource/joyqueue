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
package org.joyqueue.client.internal.producer.interceptor;

import com.google.common.collect.Maps;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;

import java.util.List;
import java.util.Map;

/**
 * ProduceContext
 *
 * author: gaohaoxiang
 * date: 2019/1/11
 */
public class ProduceContext {

    private String topic;
    private String app;
    private NameServerConfig nameserver;
    private List<ProduceMessage> messages;
    private Map<Object, Object> attributes;

    public ProduceContext(String topic, String app, NameServerConfig nameserver, List<ProduceMessage> messages) {
        this.topic = topic;
        this.app = app;
        this.nameserver = nameserver;
        this.messages = messages;
    }

    public String getTopic() {
        return topic;
    }

    public String getApp() {
        return app;
    }

    public NameServerConfig getNameserver() {
        return nameserver;
    }

    public List<ProduceMessage> getMessages() {
        return messages;
    }

    public <T> T getAttribute(Object key) {
        if (attributes == null) {
            return null;
        }
        return (T) attributes.get(key);
    }

    public boolean removeAttribute(Object key) {
        if (attributes == null) {
            return false;
        }
        return attributes.remove(key) != null;
    }

    public boolean putAttribute(Object key, Object value) {
        if (attributes == null) {
            attributes = Maps.newHashMap();
        }
        return attributes.put(key, value) == null;
    }

    public boolean containsAttribute(Object key) {
        if (attributes == null) {
            return false;
        }
        return attributes.containsKey(key);
    }

    public Map<Object, Object> getAttributes() {
        return attributes;
    }
}