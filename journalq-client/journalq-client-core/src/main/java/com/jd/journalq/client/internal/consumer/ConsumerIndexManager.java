package com.jd.journalq.client.internal.consumer;

import com.google.common.collect.Table;
import com.jd.journalq.client.internal.consumer.domain.ConsumeReply;
import com.jd.journalq.client.internal.consumer.domain.FetchIndexData;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.toolkit.lang.LifeCycle;

import java.util.List;
import java.util.Map;

/**
 * ConsumerIndexManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public interface ConsumerIndexManager extends LifeCycle {

    JMQCode resetIndex(String topic, String app, short partition, long timeout);

    FetchIndexData fetchIndex(String topic, String app, short partition, long timeout);

    JMQCode commitReply(String topic, List<ConsumeReply> replyList, String app, long timeout);

    // batch

    Table<String, Short, FetchIndexData> batchFetchIndex(Map<String, List<Short>> topicMap, String app, long timeout);

    Map<String, JMQCode> batchCommitReply(Map<String, List<ConsumeReply>> replyMap, String app, long timeout);
}