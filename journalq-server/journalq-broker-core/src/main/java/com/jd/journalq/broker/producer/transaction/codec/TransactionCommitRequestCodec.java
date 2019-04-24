package com.jd.journalq.broker.producer.transaction.codec;

import com.jd.journalq.broker.producer.transaction.command.TransactionCommitRequest;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JournalqHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

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
        transactionCommitRequest.setTxId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return transactionCommitRequest;
    }

    @Override
    public void encode(TransactionCommitRequest payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getTxId(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return CommandType.TRANSACTION_COMMIT_REQUEST;
    }
}