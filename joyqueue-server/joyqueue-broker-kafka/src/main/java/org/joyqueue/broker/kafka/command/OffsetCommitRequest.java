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
import org.joyqueue.broker.kafka.model.OffsetAndMetadata;

import java.util.List;
import java.util.Map;

/**
 * Created by zhangkepeng on 16-7-28.
 */
public class OffsetCommitRequest extends KafkaRequestOrResponse {

    public static final int DEFAULT_GENERATION_ID = -1;
    public static final String DEFAULT_CONSUMER_ID = "";
    public static final long DEFAULT_TIMESTAMP = -1L;

    private String groupId;
    private Map<String, List<OffsetAndMetadata>> offsets;
    private int groupGenerationId;
    private String memberId;
    private long retentionTime;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public int getGroupGenerationId() {
        return groupGenerationId;
    }

    public void setGroupGenerationId(int groupGenerationId) {
        this.groupGenerationId = groupGenerationId;
    }

    public void setOffsets(Map<String, List<OffsetAndMetadata>> offsets) {
        this.offsets = offsets;
    }

    public Map<String, List<OffsetAndMetadata>> getOffsets() {
        return offsets;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public long getRetentionTime() {
        return retentionTime;
    }

    public void setRetentionTime(long retentionTime) {
        this.retentionTime = retentionTime;
    }

    @Override
    public String toString() {
        return "OffsetCommitRequest{" +
                "groupId='" + groupId + '\'' +
                ", offsets=" + offsets +
                ", groupGenerationId=" + groupGenerationId +
                ", memberId='" + memberId + '\'' +
                ", retentionTime=" + retentionTime +
                '}';
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_COMMIT.getCode();
    }
}
