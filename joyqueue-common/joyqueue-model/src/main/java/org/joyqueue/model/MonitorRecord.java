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
package org.joyqueue.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lining11
 * Date: 2018/12/13
 */
public class MonitorRecord implements Cloneable, Serializable {

    private String provider;
    private String service;
    private String endpoint;
    private String metric;
    private double value;
    private String category;
    private long timestamp;//精确到s
    private Map<String, String> tags = new HashMap<>();

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        setValue(value,true);
    }

    public void setValue(double value,boolean positive) {
        if (positive){
            value = Math.abs(value);
        }
        this.value = value;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * key 必须为
     * t1,brokerId
     * t2,host
     * t3,topic
     * t4,app
     * t5,partitionGroup
     * t6,partition
     *
     * @param key   key
     * @param value value
     */
    public void addTag(String key, String value) {
        tags.put(key, value);
    }

    /**
     * key 必须为
     * t1,brokerId
     * t2,host
     * t3,topic
     * t4,app
     * t5,partitionGroup
     * t6,partition
     *
     * @param key key
     * @return 对应的tag
     */
    public String getTag(String key) {
        return tags.get(key);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void brokerId(String brokerId) {
        tags.put("t1", brokerId);
    }

    public void host(String host) {
        tags.put("t2", host);
    }

    public void topic(String topic) {
        tags.put("t3", topic);
    }

    public void app(String app) {
        tags.put("t4", app);
    }

    public void partitionGroup(String partitionGroup) {
        tags.put("t5", partitionGroup);
    }

    public void partition(String partiton) {
        tags.put("t6", partiton);
    }
}
