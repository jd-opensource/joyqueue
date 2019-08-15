package io.chubao.joyqueue.client.internal.cluster.domain;

import io.chubao.joyqueue.client.internal.metadata.domain.TopicMetadata;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.toolkit.time.SystemClock;

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