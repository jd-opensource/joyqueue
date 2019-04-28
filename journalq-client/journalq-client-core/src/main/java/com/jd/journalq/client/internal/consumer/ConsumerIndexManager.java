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
package com.jd.journalq.client.internal.consumer;

import com.google.common.collect.Table;
import com.jd.journalq.client.internal.consumer.domain.ConsumeReply;
import com.jd.journalq.client.internal.consumer.domain.FetchIndexData;
import com.jd.journalq.exception.JournalqCode;
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

    JournalqCode resetIndex(String topic, String app, short partition, long timeout);

    FetchIndexData fetchIndex(String topic, String app, short partition, long timeout);

    JournalqCode commitReply(String topic, List<ConsumeReply> replyList, String app, long timeout);

    // batch

    Table<String, Short, FetchIndexData> batchFetchIndex(Map<String, List<Short>> topicMap, String app, long timeout);

    Map<String, JournalqCode> batchCommitReply(Map<String, List<ConsumeReply>> replyMap, String app, long timeout);
}