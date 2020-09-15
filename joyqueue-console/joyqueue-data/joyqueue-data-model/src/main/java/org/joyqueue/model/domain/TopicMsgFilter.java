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

import java.util.Date;

/**
 * @author jiangnan53
 * @date 2020/3/30
 **/
public class TopicMsgFilter extends BaseModel {

    /**
     * app code
     */
    private String app;

    /**
     * topic code
     */
    private String topic;

    /**
     *  如果没有指定partition，则置为-1搜索所有的分组
     */
    private Integer partition ;
    /**
     * 过滤条件
     */
    private String filter;

    /**
     * 消息格式
     */
    private String msgFormat;
    /**
     * 查询条数
     */
    private int queryCount;
    /**
     * 允许查询总条数
     */
    private int totalCount;
    /**
     * 消息的偏移量
     */
    private long offset;

    private Date offsetStartTime;

    private Date offsetEndTime;

    private String objectKey;

    /**
     * 任务状态：
     * -1 结束
     * 0 未执行 默认
     * 1 正在执行
     */
    private int status = 0;

    private String description;

    private String url;

    private String brokerAddr;

    private String token;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getOffset() {
        return offset;
    }

    public Date getOffsetStartTime() {
        return offsetStartTime;
    }

    public void setOffsetStartTime(Date offsetStartTime) {
        this.offsetStartTime = offsetStartTime;
    }

    public Date getOffsetEndTime() {
        return offsetEndTime;
    }

    public void setOffsetEndTime(Date offsetEndTime) {
        this.offsetEndTime = offsetEndTime;
    }

    public String getBrokerAddr() {
        return brokerAddr;
    }

    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getPartition() {
        return partition;
    }

    public void setPartition(Integer partition) {
        this.partition = partition;
    }

    public String getMsgFormat() {
        return msgFormat;
    }

    public void setMsgFormat(String msgFormat) {
        this.msgFormat = msgFormat;
    }

    public int getQueryCount() {
        return queryCount;
    }

    public void setQueryCount(int queryCount) {
        this.queryCount = queryCount;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    @Override
    public String toString() {
        return "TopicMsgFilter{" +
                "app='" + app + '\'' +
                ", topic='" + topic + '\'' +
                ", partition=" + partition +
                ", filter='" + filter + '\'' +
                ", msgFormat='" + msgFormat + '\'' +
                ", queryCount=" + queryCount +
                ", offset=" + offset +
                ", offsetStartTime=" + offsetStartTime +
                ", offsetEndTime=" + offsetEndTime +
                ", objectKey='" + objectKey + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", brokerAddr='" + brokerAddr + '\'' +
                ", token='" + token + '\'' +
                ", id=" + id +
                ", createBy=" + createBy +
                ", createTime=" + createTime +
                ", updateBy=" + updateBy +
                ", updateTime=" + updateTime +
                ", status=" + status +
                '}';
    }

    public enum FilterStatus {
        ERROR(-2),
        FINISHED(-1),
        WAITING(0),
        RUNNING(1),
        UPLOADING(2),
        ;
        private int status;
        public int getStatus() {
            return status;
        }
        FilterStatus(int status) {
            this.status = status;
        }
    }
}
