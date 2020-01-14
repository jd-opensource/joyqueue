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
package org.joyqueue.client.internal.cluster.domain;

import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.toolkit.time.SystemClock;

/**
 * TopicMetadataHolder
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class TopicMetadataHolder {

    private String topic;
    private TopicMetadata topicMetadata;
    private long createTime;
    private long expireTime;
    private JoyQueueCode code;

    public TopicMetadataHolder(String topic, TopicMetadata topicMetadata, long createTime, long expireTime, JoyQueueCode code) {
        this.topic = topic;
        this.topicMetadata = topicMetadata;
        this.createTime = createTime;
        this.expireTime = expireTime;
        this.code = code;
    }

    public boolean isExpired(long timeout) {
        return (SystemClock.now() > (createTime + timeout));
    }

    public boolean isExpired() {
        return isExpired(expireTime);
    }

    public String getTopic() {
        return topic;
    }

    public TopicMetadata getTopicMetadata() {
        return topicMetadata;
    }

    public JoyQueueCode getCode() {
        return code;
    }
}