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
package com.jd.journalq.broker.index.command;

import com.jd.journalq.network.transport.command.JMQPayload;
import com.jd.journalq.network.command.CommandType;

import java.util.Map;
import java.util.Set;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class ConsumeIndexQueryRequest extends JMQPayload {
    private String app;
    private Map<String, Set<Integer>> topicPartitions;

    public ConsumeIndexQueryRequest(String app, Map<String, Set<Integer>> topicPartitions) {
        this.app = app;
        this.topicPartitions = topicPartitions;
    }

    public String getApp() {
        return app;
    }

    public Map<String, Set<Integer>> getTopicPartitions() {
        return topicPartitions;
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_QUERY_REQUEST;
    }
}
