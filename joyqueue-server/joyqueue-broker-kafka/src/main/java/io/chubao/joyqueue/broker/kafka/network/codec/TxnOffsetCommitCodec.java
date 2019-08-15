package io.chubao.joyqueue.broker.kafka.network.codec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.command.TxnOffsetCommitRequest;
import io.chubao.joyqueue.broker.kafka.command.TxnOffsetCommitResponse;
import io.chubao.joyqueue.broker.kafka.model.OffsetAndMetadata;
import io.chubao.joyqueue.broker.kafka.model.PartitionMetadataAndError;
import io.chubao.joyqueue.broker.kafka.network.KafkaHeader;
import io.chubao.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * TxnOffsetCommitCodec
 *
 * author: gaohaoxiang
 * date: 2019/4/4
 */
public class TxnOffsetCommitCodec implements KafkaPayloadCodec<TxnOffsetCommitResponse>, Type {

    @Override
    public TxnOffsetCommitRequest decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        String transactionId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        String groupId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        long producerId = buffer.readLong();
        short producerEpoch = buffer.readShort();

        int topicSize = Math.max(buffer.readInt(), 0);
        Map<String, List<OffsetAndMetadata>> partittions = Maps.newHashMapWithExpectedSize(topicSize);

        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int partitionSize = Math.max(buffer.readInt(), 0);
            List<OffsetAndMetadata> offsetAndMetadataList = Lists.newArrayListWithCapacity(partitionSize);
            for (int j = 0; j < partitionSize; j++) {
                OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata();
                offsetAndMetadata.setPartition(buffer.readInt());
                offsetAndMetadata.setOffset(buffer.readLong());

                if (header.getVersion() >= 2) {
                    offsetAndMetadata.setLeaderEpoch(buffer.readInt());
                }

                offsetAndMetadata.setMetadata(Serializer.readString(buffer, Serializer.SHORT_SIZE));
                offsetAndMetadataList.add(offsetAndMetadata);
            }

            partittions.put(topic, offsetAndMetadataList);
        }

        TxnOffsetCommitRequest txnOffsetCommitRequest = new TxnOffsetCommitRequest();
        txnOffsetCommitRequest.setTransactionId(transactionId);
        txnOffsetCommitRequest.setGroupId(groupId);
        txnOffsetCommitRequest.setProducerId(producerId);
        txnOffsetCommitRequest.setProducerEpoch(producerEpoch);
        txnOffsetCommitRequest.setPartitions(partittions);
        return txnOffsetCommitRequest;
    }

    @Override
    public void encode(TxnOffsetCommitResponse payload, ByteBuf buffer) throws Exception {
        Map<String, List<PartitionMetadataAndError>> partitions = payload.getPartitions();

        buffer.writeInt(payload.getThrottleTimeMs());
        buffer.writeInt(partitions.size());

        for (Map.Entry<String, List<PartitionMetadataAndError>> entry : partitions.entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeInt(entry.getValue().size());
            for (PartitionMetadataAndError partitionMetadataAndError : entry.getValue()) {
                buffer.writeInt(partitionMetadataAndError.getPartition());
                buffer.writeShort(partitionMetadataAndError.getError());
            }
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.TXN_OFFSET_COMMIT.getCode();
    }
}