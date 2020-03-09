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
package org.joyqueue.server.archive.store.model;

import java.util.Arrays;

/**
 * 发送日志
 * <p>
 * Created by chengzhiliang on 2018/12/4.
 */
public class SendLog {

    private String topic;
    private long sendTime;
    private String businessId;
    private String messageId;
    private int brokerId;
    private String app;
    private byte[] clientIp;
    private String clientIpStr;
    private short compressType; //预留压缩类型字段
    private byte[] messageBody;

    // 扩展字段
    private byte[] bytesMessageId;
    private int topicId;
    private int appId;
    private short partition;
    private long index;

    private String rowKeyStart;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(int brokerId) {
        this.brokerId = brokerId;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public byte[] getClientIp() {
        return clientIp;
    }

    public void setClientIp(byte[] clientIp) {
        this.clientIp = clientIp;
    }

    public String getClientIpStr() {
        return clientIpStr;
    }

    public void setClientIpStr(String clientIpStr) {
        this.clientIpStr = clientIpStr;
    }

    public short getCompressType() {
        return compressType;
    }

    public void setCompressType(short compressType) {
        this.compressType = compressType;
    }

    public byte[] getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(byte[] messageBody) {
        this.messageBody = messageBody;
    }

    public byte[] getBytesMessageId() {
        return bytesMessageId;
    }

    public void setBytesMessageId(byte[] bytesMessageId) {
        this.bytesMessageId = bytesMessageId;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public String getRowKeyStart() {
        return rowKeyStart;
    }

    public void setRowKeyStart(String rowKeyStart) {
        this.rowKeyStart = rowKeyStart;
    }

    @Override
    public String toString() {
        return "SendLog{" +
                "topic='" + topic + '\'' +
                ", sendTime=" + sendTime +
                ", messageId='" + messageId + '\'' +
                ", brokerId=" + brokerId +
                ", app='" + app + '\'' +
                ", clientIp=" + Arrays.toString(clientIp) +
                ", clientIpStr='" + clientIpStr + '\'' +
                ", topicId=" + topicId +
                ", appId=" + appId +
                ", partition=" + partition +
                ", index=" + index +
                '}';
    }
}
