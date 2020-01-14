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
package org.joyqueue.broker.producer.transaction.codec;

import com.google.common.collect.Lists;
import org.joyqueue.broker.producer.transaction.command.TransactionCommitRequest;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * TransactionCommitRequestCodec
 *
 * author: gaohaoxiang
 * date: 2019/4/12
 */
public class TransactionCommitRequestCodec implements PayloadCodec<JoyQueueHeader, TransactionCommitRequest>, Type {

    @Override
    public TransactionCommitRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
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