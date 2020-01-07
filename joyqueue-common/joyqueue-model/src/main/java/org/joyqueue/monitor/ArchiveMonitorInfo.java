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

import java.io.Serializable;
import java.util.Map;

public class ArchiveMonitorInfo implements Serializable {

    /**
     * consumer 消费待归档记录数
     **/
    private long consumeBacklog;
    /**
     * producer 生产待归档记录数
     *
     **/
    private long produceBacklog;

    /**
     * producer 安主题生产待归档记录数
     *
     **/
    private Map<String, Long> topicProduceBacklog;

    public long getConsumeBacklog() {
        return consumeBacklog;
    }

    public void setConsumeBacklog(long consumeBacklog) {
        this.consumeBacklog = consumeBacklog;
    }

    public long getProduceBacklog() {
        return produceBacklog;
    }

    public void setProduceBacklog(long produceBacklog) {
        this.produceBacklog = produceBacklog;
    }

    public Map<String, Long> getTopicProduceBacklog() {
        return topicProduceBacklog;
    }

    public void setTopicProduceBacklog(Map<String, Long> topicProduceBacklog) {
        this.topicProduceBacklog = topicProduceBacklog;
    }
}
