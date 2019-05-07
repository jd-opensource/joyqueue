/**
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
package com.jd.journalq.client.internal.cluster.domain;

import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.toolkit.time.SystemClock;

/**
 * TopicMetadataHolder
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class TopicMetadataHolder {

    private String topic;
    private TopicMetadata topicMetadata;
    private long createTime;
    private long expireTime;
    private JournalqCode code;

    public TopicMetadataHolder(String topic, TopicMetadata topicMetadata, long createTime, long expireTime, JournalqCode code) {
        this.topic = topic;
        this.topicMetadata = topicMetadata;
        this.createTime = createTime;
        this.expireTime = expireTime;
        this.code = code;
    }

    public boolean isExpired(long expireTime) {
        return (SystemClock.now() > (createTime + expireTime));
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

    public JournalqCode getCode() {
        return code;
    }
}