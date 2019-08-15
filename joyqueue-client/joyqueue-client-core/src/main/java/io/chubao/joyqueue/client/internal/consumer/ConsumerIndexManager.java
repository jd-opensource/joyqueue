package io.chubao.joyqueue.client.internal.consumer;

import com.google.common.collect.Table;
import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeReply;
import io.chubao.joyqueue.client.internal.consumer.domain.FetchIndexData;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.toolkit.lang.LifeCycle;

import java.util.List;
import java.util.Map;

/**
 * ConsumerIndexManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public interface ConsumerIndexManager extends LifeCycle {

    JoyQueueCode resetIndex(String topic, String app, short partition, long timeout);

    FetchIndexData fetchIndex(String topic, String app, short partition, long timeout);

    JoyQueueCode commitReply(String topic, List<ConsumeReply> replyList, String app, long timeout);

    // batch

    Table<String, Short, FetchIndexData> batchFetchIndex(Map<String, List<Short>> topicMap, String app, long timeout);

    Map<String, JoyQueueCode> batchCommitReply(Map<String, List<ConsumeReply>> replyMap, String app, long timeout);
}