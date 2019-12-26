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
package org.joyqueue.nsr.ignite.model;

import org.joyqueue.domain.ConsumeRetry;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

/**
 * @author wylixiaobin
 * Date: 2018/9/6
 */
public class IgniteConsumeRetry implements IgniteBaseModel {
    // 消息ID
    @QuerySqlField(name = "message_id", index = true)
    private String messageId;
    // 业务ID
    @QuerySqlField(name = "business_id")
    private String businessId;
    @QuerySqlField(name = "namespace")
    private String namespace;
    // 主题
    @QuerySqlField(name = "topic")
    private String topic;
    // 应用
    @QuerySqlField(name = "app")
    private String app;
    // 发送时间
    @QuerySqlField(name = "send_time")
    private long sendTime;
    // 过期时间
    @QuerySqlField(name = "expireTime")
    private long expireTime;
    // 重试时间
    @QuerySqlField(name = "retry_time")
    private long retryTime;
    // 重试次数
    @QuerySqlField(name = "retry_count")
    private short retryCount;
    // 消息体
    @QuerySqlField(name = "data")
    private byte[] data;
    // 异常
    @QuerySqlField(name = "exception")
    private byte[] exception;
    // 状态
    @QuerySqlField(name = "status")
    private short status;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
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

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public long getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(long retryTime) {
        this.retryTime = retryTime;
    }

    public short getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(short retryCount) {
        this.retryCount = retryCount;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getException() {
        return exception;
    }

    public void setException(byte[] exception) {
        this.exception = exception;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String getId() {
        return messageId;
    }


    public static IgniteConsumeRetry toIgniteConsumeRetry(ConsumeRetry retryMessage) {
        String[] nt = retryMessage.getTopic().split(SPLICE);
        IgniteConsumeRetry igniteRetryMessage = new IgniteConsumeRetry();
        igniteRetryMessage.setMessageId(retryMessage.getMessageId());
        igniteRetryMessage.setBusinessId(retryMessage.getBusinessId());
        igniteRetryMessage.setNamespace(nt[0]);
        igniteRetryMessage.setTopic(nt[1]);
        igniteRetryMessage.setApp(retryMessage.getApp());
        igniteRetryMessage.setData(retryMessage.getData());
        igniteRetryMessage.setException(retryMessage.getException());
        igniteRetryMessage.setSendTime(retryMessage.getSendTime());
        igniteRetryMessage.setExpireTime(retryMessage.getExpireTime());
        igniteRetryMessage.setStatus(retryMessage.getStatus());
        return igniteRetryMessage;
    }

    public static ConsumeRetry toConsumeRetry(IgniteConsumeRetry igniteRetryMessage) {
        if (null == igniteRetryMessage) return null;
        ConsumeRetry retryMessage = new ConsumeRetry();
        retryMessage.setMessageId(igniteRetryMessage.getMessageId());
        retryMessage.setBusinessId(igniteRetryMessage.getBusinessId());
        retryMessage.setTopic(igniteRetryMessage.getNamespace() + SPLICE + igniteRetryMessage.getTopic());
        retryMessage.setApp(igniteRetryMessage.getApp());
        retryMessage.setData(igniteRetryMessage.getData());
        retryMessage.setException(igniteRetryMessage.getException());
        retryMessage.setSendTime(igniteRetryMessage.getSendTime());
        retryMessage.setExpireTime(igniteRetryMessage.getExpireTime());
        retryMessage.setStatus(igniteRetryMessage.getStatus());
        return retryMessage;
    }
}
