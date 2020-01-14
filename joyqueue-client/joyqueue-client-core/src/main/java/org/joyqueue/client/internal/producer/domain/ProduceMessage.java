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
package org.joyqueue.client.internal.producer.domain;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * ProduceMessageRequest
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class ProduceMessage implements Serializable {

    public static final short NONE_PARTITION = Short.MIN_VALUE;
    public static final String NONE_PARTITION_KEY = null;

    private String topic;
    private String body;
    private byte[] bodyBytes;
    private short partition = NONE_PARTITION;
    private String partitionKey = NONE_PARTITION_KEY;
    private String businessId;
    private byte priority;
    private short flag;
    private Map<String, String> attributes;

    public ProduceMessage() {

    }

    public ProduceMessage(String topic, String body) {
        this.topic = topic;
        this.body = body;
    }

    public ProduceMessage(String topic, String body, String businessId) {
        this.topic = topic;
        this.body = body;
        this.businessId = businessId;
    }

    public ProduceMessage(String topic, String body, String businessId, String partitionKey) {
        this.topic = topic;
        this.body = body;
        this.businessId = businessId;
        this.partitionKey = partitionKey;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public byte[] getBodyBytes() {
        return bodyBytes;
    }

    public void setBodyBytes(byte[] bodyBytes) {
        this.bodyBytes = bodyBytes;
    }

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public byte getPriority() {
        return priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    public void setFlag(short flag) {
        this.flag = flag;
    }

    public short getFlag() {
        return flag;
    }

    public Map<String, String> getAttributes() {
        if (attributes == null) {
            return Collections.emptyMap();
        }
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public boolean putAttribute(String key, String value) {
        if (attributes == null) {
            attributes = Maps.newHashMap();
        }
        return attributes.put(key, value) != null;
    }

    public String getAttribute(String key) {
        if (attributes == null) {
            return null;
        }
        return attributes.get(key);
    }

    public boolean containsAttribute(String key) {
        if (attributes == null) {
            return false;
        }
        return attributes.containsKey(key);
    }

    @Override
    public String toString() {
        return "ProduceMessage{" +
                "topic='" + topic + '\'' +
                ", partition=" + partition +
                ", partitionKey='" + partitionKey + '\'' +
                ", businessId='" + businessId + '\'' +
                ", flag='" + flag + '\'' +
                '}';
    }
}