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
package org.joyqueue.monitor;

import org.joyqueue.message.BrokerMessage;

import java.util.Base64;
import java.util.Map;

/**
 * BrokerMessageInfo
 *
 * author: gaohaoxiang
 * date: 2018/11/29
 */
public class BrokerMessageInfo extends BaseMonitorInfo {

    private long msgIndexNo;
    private int storeTime;
    private long startTime;
    private byte source;
    private short partition;
    private String topic;
    private String app;
    private String businessId;
    private byte priority;
    private long bodyCRC;
    private String body;
    private Map<String, String> attributes;
    private boolean ack;

    public BrokerMessageInfo() {

    }

    public BrokerMessageInfo(BrokerMessage message) {
        this(message, false);
    }

    public BrokerMessageInfo(BrokerMessage message, boolean ack) {
        this.msgIndexNo = message.getMsgIndexNo();
        this.storeTime = message.getStoreTime();
        this.startTime = message.getStartTime();
        this.source = message.getSource();
        this.partition = message.getPartition();
        this.topic = message.getTopic();
        this.app = message.getApp();
        this.businessId = message.getBusinessId();
        this.priority = message.getPriority();
        this.bodyCRC = message.getBodyCRC();
        byte[] bytes = message.getDecompressedBody();
        if(bytes!=null&&bytes.length>0) {
            this.body = Base64.getEncoder().encodeToString(bytes);
        }
        this.attributes = message.getAttributes();
        this.ack = ack;
    }

    public long getMsgIndexNo() {
        return msgIndexNo;
    }

    public void setMsgIndexNo(long msgIndexNo) {
        this.msgIndexNo = msgIndexNo;
    }

    public int getStoreTime() {
        return storeTime;
    }

    public void setStoreTime(int storeTime) {
        this.storeTime = storeTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public byte getSource() {
        return source;
    }

    public void setSource(byte source) {
        this.source = source;
    }

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
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

    public long getBodyCRC() {
        return bodyCRC;
    }

    public void setBodyCRC(long bodyCRC) {
        this.bodyCRC = bodyCRC;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public boolean isAck() {
        return ack;
    }
}