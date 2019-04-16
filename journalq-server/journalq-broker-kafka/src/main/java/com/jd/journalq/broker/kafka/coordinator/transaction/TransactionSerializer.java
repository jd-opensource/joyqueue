package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMarker;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionState;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.toolkit.lang.Charsets;

import java.nio.ByteBuffer;

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
}