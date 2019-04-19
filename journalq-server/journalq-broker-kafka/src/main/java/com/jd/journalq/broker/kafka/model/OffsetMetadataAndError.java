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
package com.jd.journalq.broker.kafka.model;


import com.jd.journalq.broker.kafka.KafkaErrorCode;

/**
 * Created by zhangkepeng on 16-8-4.
 */
public class OffsetMetadataAndError {

    private static final OffsetMetadataAndError noOffset = new OffsetMetadataAndError(OffsetAndMetadata.INVALID_OFFSET, OffsetAndMetadata.NO_METADATA, KafkaErrorCode.NONE);
    private static final OffsetMetadataAndError offsetsLoading = new OffsetMetadataAndError(OffsetAndMetadata.INVALID_OFFSET, OffsetAndMetadata.NO_METADATA, KafkaErrorCode.OFFSETS_LOAD_IN_PROGRESS);
    private static final OffsetMetadataAndError notOffsetManagerForGroup = new OffsetMetadataAndError(OffsetAndMetadata.INVALID_OFFSET, OffsetAndMetadata.NO_METADATA, KafkaErrorCode.NOT_COORDINATOR_FOR_CONSUMER);
    private static final OffsetMetadataAndError unknownTopicOrPartition = new OffsetMetadataAndError(OffsetAndMetadata.INVALID_OFFSET, OffsetAndMetadata.NO_METADATA, KafkaErrorCode.UNKNOWN_TOPIC_OR_PARTITION);

    private long offset;
    private String metadata = OffsetAndMetadata.NO_METADATA;
    private short error = KafkaErrorCode.NONE;

    public static final OffsetMetadataAndError OFFSET_SYNC_FAIL = new OffsetMetadataAndError(OffsetAndMetadata.INVALID_OFFSET, OffsetAndMetadata.NO_METADATA, KafkaErrorCode.NOT_LEADER_FOR_PARTITION);
    public static final OffsetMetadataAndError OFFSET_SYNC_SUCCESS = new OffsetMetadataAndError(OffsetAndMetadata.INVALID_OFFSET, OffsetAndMetadata.NO_METADATA, KafkaErrorCode.NONE);

    public OffsetMetadataAndError(short error) {
        this.error = error;
    }

    public OffsetMetadataAndError(long offset, String metadata, short error) {
        this.offset = offset;
        this.metadata = metadata;
        this.error = error;
    }

    public long getOffset() {
        return offset;
    }

    public String getMetadata() {
        return metadata;
    }

    public short getError() {
        return error;
    }

    @Override
    public String toString() {
        return String.format("OffsetMetadataAndError[%d,%s,%d]", offset, metadata, error);
    }

}