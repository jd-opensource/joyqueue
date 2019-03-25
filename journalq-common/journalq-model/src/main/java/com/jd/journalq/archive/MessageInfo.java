package com.jd.journalq.archive;

import java.util.Date;

/**
 * 归档消息
 */
public class MessageInfo implements ArchiveMessage {
    // 记录行ID
    private String id;
    // 消息id，包括了主机地址
    private String messageId;
    // 主题
    private String topic;
    // 应用
    private String app;
    // 业务ID
    private String businessId;
    // 发送消息的主机IP
    private String clientIp;
    // 客户端发送时间
    private long sendTime;
    // 送达中间件时间
    private long receiveTime;
    // 创建时间
    private long createTime;
    // 存储文件key
    private String fileKey;
    // 存储文件偏移量
    private int offset = -1;
    // 日志偏移量
    private long journalOffset;
    // 日志大小
    private int journalSize;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Date getArchiveTime() {
        return new Date(receiveTime);
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
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

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public long getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(long receiveTime) {
        this.receiveTime = receiveTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getJournalSize() {
        return journalSize;
    }

    public void setJournalSize(int journalSize) {
        this.journalSize = journalSize;
    }

    public long getJournalOffset() {
        return journalOffset;
    }

    public void setJournalOffset(long journalOffset) {
        this.journalOffset = journalOffset;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageInfo{");
        sb.append("messageId='").append(messageId).append('\'');
        sb.append(", topic='").append(topic).append('\'');
        sb.append(", app='").append(app).append('\'');
        sb.append(", businessId='").append(businessId).append('\'');
        sb.append(", clientIp='").append(clientIp).append('\'');
        sb.append(", sendTime=").append(sendTime);
        sb.append(", receiveTime=").append(receiveTime);
        sb.append(", createTime=").append(createTime);
        sb.append(", fileKey='").append(fileKey).append('\'');
        sb.append(", offset=").append(offset);
        sb.append(", journalOffset=").append(journalOffset);
        sb.append(", journalSize=").append(journalSize);
        sb.append('}');
        return sb.toString();
    }
}
