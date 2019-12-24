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
package org.joyqueue.broker.kafka.command;

import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.model.OffsetMetadataAndError;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangkepeng on 16-8-4.
 */
public class OffsetFetchResponse extends KafkaRequestOrResponse {

    private Map<String, List<OffsetMetadataAndError>> topicMetadataAndErrors;

    public OffsetFetchResponse() {

    }

    public OffsetFetchResponse(Map<String, List<OffsetMetadataAndError>> topicMetadataAndErrors) {
        this.topicMetadataAndErrors = topicMetadataAndErrors;
    }

    public void setTopicMetadataAndErrors(Map<String, List<OffsetMetadataAndError>> topicMetadataAndErrors) {
        this.topicMetadataAndErrors = topicMetadataAndErrors;
    }

    public Map<String, List<OffsetMetadataAndError>> getTopicMetadataAndErrors() {
        return topicMetadataAndErrors;
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_FETCH.getCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Name: " + this.getClass().getSimpleName());
        return builder.toString();
    }
}
