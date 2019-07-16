/**
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
package com.jd.joyqueue.broker.producer.transaction.codec;

import com.google.common.collect.Lists;
import com.jd.joyqueue.broker.producer.transaction.command.TransactionRollbackRequest;
import com.jd.joyqueue.network.command.CommandType;
import com.jd.joyqueue.network.serializer.Serializer;
import com.jd.joyqueue.network.transport.codec.JoyQueueHeader;
import com.jd.joyqueue.network.transport.codec.PayloadCodec;
import com.jd.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * TransactionRollbackRequestCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/12
 */
public class TransactionRollbackRequestCodec implements PayloadCodec<JoyQueueHeader, TransactionRollbackRequest>, Type {

    @Override
    public TransactionRollbackRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        TransactionRollbackRequest transactionRollbackRequest = new TransactionRollbackRequest();
        transactionRollbackRequest.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        transactionRollbackRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));

        int txIdSize = buffer.readShort();
        List<String> txIds = Lists.newArrayListWithCapacity(txIdSize);
        for (int i = 0; i < txIdSize; i++) {
            txIds.add(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }
        transactionRollbackRequest.setTxIds(txIds);
        return transactionRollbackRequest;
    }

    @Override
    public void encode(TransactionRollbackRequest payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);

        buffer.writeShort(payload.getTxIds().size());
        for (String txId : payload.getTxIds()) {
            Serializer.write(txId, buffer, Serializer.SHORT_SIZE);
        }
    }

    @Override
    public int type() {
        return CommandType.TRANSACTION_ROLLBACK_REQUEST;
    }
}