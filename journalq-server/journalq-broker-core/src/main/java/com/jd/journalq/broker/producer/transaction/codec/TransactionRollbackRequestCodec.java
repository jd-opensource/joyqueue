package com.jd.journalq.broker.producer.transaction.codec;

import com.jd.journalq.broker.producer.transaction.command.TransactionRollbackRequest;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * TransactionRollbackRequestCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/12
 */
public class TransactionRollbackRequestCodec implements PayloadCodec<JMQHeader, TransactionRollbackRequest>, Type {

    @Override
    public TransactionRollbackRequest decode(JMQHeader header, ByteBuf buffer) throws Exception {
        TransactionRollbackRequest transactionRollbackRequest = new TransactionRollbackRequest();
        transactionRollbackRequest.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        transactionRollbackRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        transactionRollbackRequest.setTxId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return transactionRollbackRequest;
    }

    @Override
    public void encode(TransactionRollbackRequest payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getTxId(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return CommandType.TRANSACTION_ROLLBACK_REQUEST;
    }
}