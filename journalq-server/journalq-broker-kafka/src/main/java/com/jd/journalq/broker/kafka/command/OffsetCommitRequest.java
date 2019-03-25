package com.jd.journalq.broker.kafka.command;


import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.model.OffsetAndMetadata;

/**
 * Created by zhangkepeng on 16-7-28.
 */
public class OffsetCommitRequest extends KafkaRequestOrResponse {

    public static final int DEFAULT_GENERATION_ID = -1;
    public static final String DEFAULT_CONSUMER_ID = "";
    public static final long DEFAULT_TIMESTAMP = -1L;
    private String groupId;
    private Table<String, Integer, OffsetAndMetadata> offsetAndMetadata;
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

    public Table<String, Integer, OffsetAndMetadata> getOffsetAndMetadata() {
        return offsetAndMetadata;
    }

    public void setOffsetAndMetadata(Table<String, Integer, OffsetAndMetadata> offsetAndMetadata) {
        this.offsetAndMetadata = offsetAndMetadata;
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
        StringBuilder offsetCommitRequest = new StringBuilder();
        offsetCommitRequest.append("Name: " + this.getClass().getSimpleName());
        offsetCommitRequest.append("; GroupId: " + groupId);
        offsetCommitRequest.append("; GroupGenerationId: " + groupGenerationId);
        offsetCommitRequest.append("; memberId: " + memberId);
        return offsetCommitRequest.toString();
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_COMMIT.getCode();
    }
}
