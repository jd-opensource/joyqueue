package io.chubao.joyqueue.broker.kafka.command;


import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.model.OffsetAndMetadata;

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
