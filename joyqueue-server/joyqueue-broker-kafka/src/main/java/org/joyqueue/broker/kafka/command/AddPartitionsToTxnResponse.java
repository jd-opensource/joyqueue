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
import org.joyqueue.broker.kafka.model.PartitionMetadataAndError;

import java.util.List;
import java.util.Map;

/**
 * AddPartitionsToTxnResponse
 *
 * author: gaohaoxiang
 * date: 2019/4/4
 */
public class AddPartitionsToTxnResponse extends KafkaRequestOrResponse {

    private Map<String, List<PartitionMetadataAndError>> errors;

    public AddPartitionsToTxnResponse() {

    }

    public AddPartitionsToTxnResponse(Map<String, List<PartitionMetadataAndError>> errors) {
        this.errors = errors;
    }

    public void setErrors(Map<String, List<PartitionMetadataAndError>> errors) {
        this.errors = errors;
    }

    public Map<String, List<PartitionMetadataAndError>> getErrors() {
        return errors;
    }

    @Override
    public int type() {
        return KafkaCommandType.ADD_PARTITIONS_TO_TXN.getCode();
    }
}