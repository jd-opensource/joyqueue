package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMarker;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionOffset;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionState;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.toolkit.lang.Charsets;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * TransactionSerializer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/15
 */
public class TransactionSerializer {

    private static final int PREPARE_FIX_LENGTH =
                    + 2 // topic length
                    + 2 // partition
                    + 2 // app length
                    + 4 // brokerId
                    + 1 // broker host length
                    + 4 // brokerPort
                    + 2 // transactionId length
                    + 8 // producerId
                    + 2 // producerEpoch
                    + 4 // timeout
                    + 8 // createTime
            ;

    private static final int MARKER_FIX_LENGTH =
                    + 2 // app length
                    + 2 // transactionId length
                    + 8 // producerId
                    + 2 // producerEpoch
                    + 4 // timeout
                    + 1 // state
                    + 8 // createTime
            ;

    private static final int OFFSET_FIX_LENGTH =
                    + 2 // topic length
                    + 2 // partition length
                    + 8 // offset length
                    + 2 // app length
                    + 2 // transactionId length
                    + 8 // producerId
                    + 2 // producerEpoch
                    + 4 // timeout
                    + 8 // createTime
            ;

    public static int sizeOfPrepare(TransactionPrepare prepare) throws Exception {
        int size = PREPARE_FIX_LENGTH;

        byte[] bytes = Serializer.getBytes(prepare.getTopic(), Charsets.UTF_8);
        size += bytes == null? 0: bytes.length;

        bytes = Serializer.getBytes(prepare.getApp(), Charsets.UTF_8);
        size += bytes == null? 0: bytes.length;

        bytes = Serializer.getBytes(prepare.getBrokerHost(), Charsets.UTF_8);
        size += bytes == null? 0: bytes.length;

        bytes = Serializer.getBytes(prepare.getTransactionId(), Charsets.UTF_8);
        size += bytes == null? 0: bytes.length;

        return size;
    }

    public static byte[] serializePrepare(TransactionPrepare prepare) throws Exception {
        int size = sizeOfPrepare(prepare);
        ByteBuffer buffer = ByteBuffer.allocate(size);

        Serializer.write(prepare.getTopic(), buffer, Serializer.SHORT_SIZE);
        buffer.putShort(prepare.getPartition());
        Serializer.write(prepare.getApp(), buffer, Serializer.SHORT_SIZE);
        buffer.putInt(prepare.getBrokerId());
        Serializer.write(prepare.getBrokerHost(), buffer, Serializer.BYTE_SIZE);
        buffer.putInt(prepare.getBrokerPort());
        Serializer.write(prepare.getTransactionId(), buffer, Serializer.BYTE_SIZE);
        buffer.putLong(prepare.getProducerId());
        buffer.putShort(prepare.getProducerEpoch());
        buffer.putInt(prepare.getTimeout());
        buffer.putLong(prepare.getCreateTime());
        return buffer.array();
    }

    public static TransactionPrepare deserializePrepare(byte[] value) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(value);
        TransactionPrepare prepare = new TransactionPrepare();
        prepare.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        prepare.setPartition(buffer.getShort());
        prepare.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        prepare.setBrokerId(buffer.getInt());
        prepare.setBrokerHost(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        prepare.setBrokerPort(buffer.getInt());
        prepare.setTransactionId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        prepare.setProducerId(buffer.getLong());
        prepare.setProducerEpoch(buffer.getShort());
        prepare.setTimeout(buffer.getInt());
        prepare.setCreateTime(buffer.getLong());
        return prepare;
    }

    public static int sizeOfMarker(TransactionMarker marker) throws Exception {
        int size = MARKER_FIX_LENGTH;

        byte[] bytes = Serializer.getBytes(marker.getApp(), Charsets.UTF_8);
        size += bytes == null? 0: bytes.length;

        bytes = Serializer.getBytes(marker.getTransactionId(), Charsets.UTF_8);
        size += bytes == null? 0: bytes.length;

        return size;
    }

    public static byte[] serializeMarker(TransactionMarker marker) throws Exception {
        int size = sizeOfMarker(marker);
        ByteBuffer buffer = ByteBuffer.allocate(size);

        Serializer.write(marker.getApp(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(marker.getTransactionId(), buffer, Serializer.BYTE_SIZE);
        buffer.putLong(marker.getProducerId());
        buffer.putShort(marker.getProducerEpoch());
        buffer.put((byte) marker.getState().getValue());
        buffer.putInt(marker.getTimeout());
        buffer.putLong(marker.getCreateTime());
        return buffer.array();
    }

    public static TransactionMarker deserializeMarker(byte[] value) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(value);
        TransactionMarker marker = new TransactionMarker();
        marker.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        marker.setTransactionId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        marker.setProducerId(buffer.getLong());
        marker.setProducerEpoch(buffer.getShort());
        marker.setState(TransactionState.valueOf(buffer.get()));
        marker.setTimeout(buffer.getInt());
        marker.setCreateTime(buffer.getLong());
        return marker;
    }

    public static int sizeOfOffsets(Map<String, List<TransactionOffset>> offsets) throws Exception {
        int size = 0;
        size += Serializer.INT_SIZE;
        for (Map.Entry<String, List<TransactionOffset>> entry : offsets.entrySet()) {
            size += Serializer.INT_SIZE;
            size += Serializer.SHORT_SIZE;
            byte[] bytes = Serializer.getBytes(entry.getKey(), Charsets.UTF_8);
            size += bytes == null? 0: bytes.length;

            for (TransactionOffset offset : entry.getValue()) {
                size += OFFSET_FIX_LENGTH;

                bytes = Serializer.getBytes(offset.getTopic(), Charsets.UTF_8);
                size += bytes == null? 0: bytes.length;

                bytes = Serializer.getBytes(offset.getApp(), Charsets.UTF_8);
                size += bytes == null? 0: bytes.length;

                bytes = Serializer.getBytes(offset.getTransactionId(), Charsets.UTF_8);
                size += bytes == null? 0: bytes.length;
            }
        }
        return size;
    }

    public static byte[] serializeOffsets(Map<String, List<TransactionOffset>> offsets) throws Exception {
        int size = sizeOfOffsets(offsets);
        ByteBuffer buffer = ByteBuffer.allocate(size);

        buffer.putInt(offsets.size());
        for (Map.Entry<String, List<TransactionOffset>> entry : offsets.entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.putInt(entry.getValue().size());

            for (TransactionOffset offset : entry.getValue()) {
                Serializer.write(offset.getTopic(), buffer, Serializer.SHORT_SIZE);
                buffer.putShort(offset.getPartition());
                buffer.putLong(offset.getOffset());
                Serializer.write(offset.getApp(), buffer, Serializer.SHORT_SIZE);
                Serializer.write(offset.getTransactionId(), buffer, Serializer.SHORT_SIZE);
                buffer.putLong(offset.getProducerId());
                buffer.putShort(offset.getProducerEpoch());
                buffer.putInt(offset.getTimeout());
                buffer.putLong(offset.getCreateTime());
            }
        }
        return buffer.array();
    }

    public static Map<String, List<TransactionOffset>> deserializeOffsets(byte[] value) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(value);
        int size = buffer.getInt();
        Map<String, List<TransactionOffset>> result = Maps.newHashMapWithExpectedSize(size);

        for (int i = 0; i < size; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int partitionSize = buffer.getInt();
            List<TransactionOffset> partitionOffsets = Lists.newArrayListWithCapacity(partitionSize);

            for (int j = 0; j < partitionSize; j++) {
                TransactionOffset transactionOffset = new TransactionOffset();
                transactionOffset.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
                transactionOffset.setPartition(buffer.getShort());
                transactionOffset.setOffset(buffer.getLong());
                transactionOffset.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
                transactionOffset.setTransactionId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
                transactionOffset.setProducerId(buffer.getLong());
                transactionOffset.setProducerEpoch(buffer.getShort());
                transactionOffset.setTimeout(buffer.getInt());
                transactionOffset.setCreateTime(buffer.getLong());
                partitionOffsets.add(transactionOffset);
            }

            result.put(topic, partitionOffsets);
        }

        return result;
    }
}