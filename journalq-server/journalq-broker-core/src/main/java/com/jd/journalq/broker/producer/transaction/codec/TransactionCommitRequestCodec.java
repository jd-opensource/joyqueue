package com.jd.journalq.broker.producer.transaction.codec;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.producer.transaction.command.TransactionCommitRequest;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JournalqHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * TransactionCommitRequestCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/12
 */
public class TransactionCommitRequestCodec implements PayloadCodec<JournalqHeader, TransactionCommitRequest>, Type {

    @Override
    public TransactionCommitRequest decode(JournalqHeader header, ByteBuf buffer) throws Exception {
        TransactionCommitRequest transactionCommitRequest = new TransactionCommitRequest();
        transactionCommitRequest.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        transactionCommitRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));

        int txIdSize = buffer.readShort();
        List<String> txIds = Lists.newArrayListWithCapacity(txIdSize);
        for (int i = 0; i < txIdSize; i++) {
            txIds.add(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }
        transactionCommitRequest.setTxIds(txIds);
        return transactionCommitRequest;
    }

    @Override
    public void encode(TransactionCommitRequest payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);

        buffer.writeShort(payload.getTxIds().size());
        for (String txId : payload.getTxIds()) {
            Serializer.write(txId, buffer, Serializer.SHORT_SIZE);
        }
    }

    @Override
    public int type() {
        return CommandType.TRANSACTION_COMMIT_REQUEST;
    }
}