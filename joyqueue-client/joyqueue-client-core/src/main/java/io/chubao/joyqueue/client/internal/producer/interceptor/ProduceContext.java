package io.chubao.joyqueue.client.internal.producer.interceptor;

import com.google.common.collect.Maps;
import io.chubao.joyqueue.client.internal.nameserver.NameServerConfig;
import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;

import java.util.List;
import java.util.Map;

/**
 * ProduceContext
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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