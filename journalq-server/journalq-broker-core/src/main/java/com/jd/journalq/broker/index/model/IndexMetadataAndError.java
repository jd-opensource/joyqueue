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
package com.jd.journalq.broker.index.model;

import com.jd.journalq.exception.JMQCode;

/**
 * Created by zhuduohui on 2018/9/17.
 */
public class IndexMetadataAndError {
    //public static final IndexMetadataAndError noOffset = new IndexMetadataAndError(IndexAndMetadata.INVALID_INDEX, IndexAndMetadata.NO_METADATA, ErrorCode.NO_ERROR);
    //public static final IndexMetadataAndError offsetsLoading = new IndexMetadataAndError(IndexAndMetadata.INVALID_INDEX, IndexAndMetadata.NO_METADATA, ErrorCode.OFFSETS_LOAD_INPROGRESS);
    //public static final IndexMetadataAndError notOffsetManagerForGroup = new IndexMetadataAndError(IndexAndMetadata.INVALID_INDEX, IndexAndMetadata.NO_METADATA, ErrorCode.NOT_COORDINATOR_FOR_CONSUMER);
    //public static final IndexMetadataAndError unknownTopicOrPartition = new IndexMetadataAndError(IndexAndMetadata.INVALID_INDEX, IndexAndMetadata.NO_METADATA, ErrorCode.UNKNOWN_TOPIC_OR_PARTITION);
    //public static final IndexMetadataAndError GroupCoordinatorNotAvailable = new IndexMetadataAndError(IndexAndMetadata.INVALID_INDEX, IndexAndMetadata.NO_METADATA, ErrorCode.GROUP_COORDINATOR_NOT_AVAILABLE);

    private long index;
    private String metadata = IndexAndMetadata.NO_METADATA;
    private short error = (short)JMQCode.SUCCESS.getCode();


    public IndexMetadataAndError(long index, String metadata, short error) {

        this.index = index;
        this.metadata = metadata;
        this.error = error;
    }

    public long getIndex() {
        return index;
    }

    public String getMetadata() {
        return metadata;
    }

    public short getError() {
        return error;
    }

    @Override
    public String toString() {
        return String.format("IndexMetadataAndError[%d,%s,%d]",index, metadata, error);
    }

}
