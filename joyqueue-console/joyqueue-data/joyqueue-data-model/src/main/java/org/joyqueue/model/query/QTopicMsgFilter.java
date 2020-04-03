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

    private String app;

    /**
     * 搜索的topic
     */
    private String topic;

    /**
     * 从某个{@param timestamp}开始消费
     */
    private long timestamp;

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

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
