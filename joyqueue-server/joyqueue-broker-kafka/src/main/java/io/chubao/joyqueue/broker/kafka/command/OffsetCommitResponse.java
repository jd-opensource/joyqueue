package io.chubao.joyqueue.broker.kafka.command;

import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.model.OffsetMetadataAndError;

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
