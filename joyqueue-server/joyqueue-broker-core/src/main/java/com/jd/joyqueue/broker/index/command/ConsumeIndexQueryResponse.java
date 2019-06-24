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
package com.jd.joyqueue.broker.index.command;

import com.jd.joyqueue.network.transport.command.JoyQueuePayload;
import com.jd.joyqueue.network.command.CommandType;
import com.jd.joyqueue.broker.index.model.IndexMetadataAndError;


import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class ConsumeIndexQueryResponse extends JoyQueuePayload {
    Map<String, Map<Integer, IndexMetadataAndError>> topicPartitionIndexs;

    public ConsumeIndexQueryResponse(Map<String, Map<Integer, IndexMetadataAndError>> topicPartitionIndexs) {
        this.topicPartitionIndexs = topicPartitionIndexs;
    }

    public Map<String, Map<Integer, IndexMetadataAndError>> getTopicPartitionIndex() {
        return topicPartitionIndexs;
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_QUERY_RESPONSE;
    }
}
