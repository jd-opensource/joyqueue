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
package com.jd.journalq.broker.kafka.command;

import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.model.OffsetMetadataAndError;

/**
 * Created by zhangkepeng on 16-8-4.
 */
public class OffsetCommitResponse extends KafkaRequestOrResponse {

    private Table<String, Integer, OffsetMetadataAndError> commitStatus;

    public OffsetCommitResponse(Table<String, Integer, OffsetMetadataAndError> commitStatus) {
        this.commitStatus = commitStatus;
    }

    public Table<String, Integer, OffsetMetadataAndError> getCommitStatus() {
        return commitStatus;
    }

    public void setCommitStatus(Table<String, Integer, OffsetMetadataAndError> commitStatus) {
        this.commitStatus = commitStatus;
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_COMMIT.getCode();
    }
}
