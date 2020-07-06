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
 * 消息消费日志
 * <p>
 * Created by chengzhiliang on 2018/12/4.
 */
public class ConsumeLog {
    private byte[] bytesMessageId; // MD5(topic+partition+index)
    private int appId;
    private int brokerId;
    private byte[] clientIp;
    private String clientIpStr;
    private long consumeTime;

    // 扩展字段
    private String messageId;
    private String topic;
    private String app;

    //16个字节messageId + 4个字节appId + 4个字节brokerId + 16个字节clientIP + 8个字节消费时间
    public static final int len = 16 + 4 + 4 + 16 + 8;
    public static final int keyLen = 16 + 4;
    public static final int valLen = 4 + 16 + 8;

    public ConsumeLog() {
    }


    public byte[] getBytesMessageId() {
        return bytesMessageId;
    }

    public void setBytesMessageId(byte[] bytesMessageId) {
        this.bytesMessageId = bytesMessageId;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public int getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(int brokerId) {
        this.brokerId = brokerId;
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

    public long getConsumeTime() {
        return consumeTime;
    }

    public void setConsumeTime(long consumeTime) {
        this.consumeTime = consumeTime;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
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

    @Override
    public String toString() {
        return "ConsumeLog{" +
                "bytesMessageId=" + Arrays.toString(bytesMessageId) +
                ", appId=" + appId +
                ", brokerId=" + brokerId +
                ", clientIp=" + Arrays.toString(clientIp) +
                ", clientIpStr='" + clientIpStr + '\'' +
                ", consumeTime=" + consumeTime +
                ", messageId='" + messageId + '\'' +
                ", topic='" + topic + '\'' +
                ", app='" + app + '\'' +
                '}';
    }
}
