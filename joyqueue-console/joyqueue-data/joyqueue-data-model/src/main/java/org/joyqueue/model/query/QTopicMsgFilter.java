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
package org.joyqueue.model.query;

import org.joyqueue.model.QKeyword;
import org.joyqueue.toolkit.time.SystemClock;

/**
 * @author jiangnan53
 * @date 2020/3/30
 **/
public class QTopicMsgFilter extends QKeyword {
    /**
     * 过滤内容
     */
    private String filter;

    private Integer partition;

    private String app;

    private String token;

    /**
     * 搜索的topic
     */
    private String topic;
    /**
     * 消息格式
     */
    private String msgFormat;

    private int queryCount;

    private int totalCount;

    private long offset;

    private long offsetStartTime;

    private long offsetEndTime;

    private String description;

    private int status = 0;

    /**
     * 请求发送时间
     */
    private long queryTime = SystemClock.now();

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getQueryTime() {
        return queryTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public long getOffsetStartTime() {
        return offsetStartTime;
    }

    public void setOffsetStartTime(long offsetStartTime) {
        this.offsetStartTime = offsetStartTime;
    }

    public long getOffsetEndTime() {
        return offsetEndTime;
    }

    public void setOffsetEndTime(long offsetEndTime) {
        this.offsetEndTime = offsetEndTime;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
