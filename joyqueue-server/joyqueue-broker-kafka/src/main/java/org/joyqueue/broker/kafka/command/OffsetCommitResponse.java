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
public class OffsetCommitResponse extends KafkaRequestOrResponse {

    private Map<String, List<OffsetMetadataAndError>> offsets;

    public OffsetCommitResponse(Map<String, List<OffsetMetadataAndError>> offsets) {
        this.offsets = offsets;
    }

    public void setOffsets(Map<String, List<OffsetMetadataAndError>> offsets) {
        this.offsets = offsets;
    }

    public Map<String, List<OffsetMetadataAndError>> getOffsets() {
        return offsets;
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_COMMIT.getCode();
    }
}
