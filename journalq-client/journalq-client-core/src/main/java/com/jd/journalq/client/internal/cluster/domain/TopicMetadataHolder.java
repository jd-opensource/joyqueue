package com.jd.journalq.client.internal.cluster.domain;

import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.exception.JMQCode;
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
    private JMQCode code;

    public TopicMetadataHolder(String topic, TopicMetadata topicMetadata, long createTime, long expireTime, JMQCode code) {
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

    public JMQCode getCode() {
        return code;
    }
}