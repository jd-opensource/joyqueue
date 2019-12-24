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
package org.joyqueue.broker.network.codec;

import org.joyqueue.broker.election.network.codec.AppendEntriesRequestDecoder;
import org.joyqueue.broker.election.network.codec.AppendEntriesRequestEncoder;
import org.joyqueue.broker.election.network.codec.AppendEntriesResponseDecoder;
import org.joyqueue.broker.election.network.codec.AppendEntriesResponseEncoder;
import org.joyqueue.broker.election.network.codec.ReplicateConsumePosRequestDecoder;
import org.joyqueue.broker.election.network.codec.ReplicateConsumePosRequestEncoder;
import org.joyqueue.broker.election.network.codec.ReplicateConsumePosResponseDecoder;
import org.joyqueue.broker.election.network.codec.ReplicateConsumePosResponseEncoder;
import org.joyqueue.broker.election.network.codec.TimeoutNowRequestDecoder;
import org.joyqueue.broker.election.network.codec.TimeoutNowRequestEncoder;
import org.joyqueue.broker.election.network.codec.TimeoutNowResponseDecoder;
import org.joyqueue.broker.election.network.codec.TimeoutNowResponseEncoder;
import org.joyqueue.broker.election.network.codec.VoteRequestDecoder;
import org.joyqueue.broker.election.network.codec.VoteRequestEncoder;
import org.joyqueue.broker.election.network.codec.VoteResponseDecoder;
import org.joyqueue.broker.election.network.codec.VoteResponseEncoder;
import org.joyqueue.broker.index.network.codec.IndexQueryRequestDecoder;
import org.joyqueue.broker.index.network.codec.IndexQueryRequestEncoder;
import org.joyqueue.broker.index.network.codec.IndexQueryResponseDecoder;
import org.joyqueue.broker.index.network.codec.IndexQueryResponseEncoder;
import org.joyqueue.broker.index.network.codec.IndexStoreRequestDecoder;
import org.joyqueue.broker.index.network.codec.IndexStoreRequestEncoder;
import org.joyqueue.broker.index.network.codec.IndexStoreResponseDecoder;
import org.joyqueue.broker.index.network.codec.IndexStoreResponseEncoder;
import org.joyqueue.broker.producer.transaction.codec.TransactionCommitRequestCodec;
import org.joyqueue.broker.producer.transaction.codec.TransactionRollbackRequestCodec;
import org.joyqueue.network.codec.BooleanAckCodec;
import org.joyqueue.network.transport.codec.PayloadCodecFactory;
import org.joyqueue.nsr.network.codec.OperatePartitionGroupCodec;
import org.joyqueue.server.retry.remote.command.codec.GetRetryAckCodec;
import org.joyqueue.server.retry.remote.command.codec.GetRetryCodec;
import org.joyqueue.server.retry.remote.command.codec.GetRetryCountAckCodec;
import org.joyqueue.server.retry.remote.command.codec.GetRetryCountCodec;
import org.joyqueue.server.retry.remote.command.codec.PutRetryCodec;
import org.joyqueue.server.retry.remote.command.codec.UpdateRetryCodec;

/**
 * BrokerPayloadCodecRegistrar
 *
 * author: gaohaoxiang
 * date: 2018/8/21
 */
// 使用BrokerPayloadCodec接口通过spi方式注册
@Deprecated
public class BrokerPayloadCodecRegistrar {

    public static PayloadCodecFactory register(PayloadCodecFactory payloadCodecFactory) {
        // boolean ack command codec
        payloadCodecFactory.register(new BooleanAckCodec());

        // retry message command codec
        payloadCodecFactory.register(new GetRetryCodec());
        payloadCodecFactory.register(new GetRetryAckCodec());
        payloadCodecFactory.register(new GetRetryCountCodec());
        payloadCodecFactory.register(new GetRetryCountAckCodec());
        payloadCodecFactory.register(new PutRetryCodec());
        payloadCodecFactory.register(new UpdateRetryCodec());

        // raft election command codec
        payloadCodecFactory.register(new VoteRequestDecoder());
        payloadCodecFactory.register(new VoteRequestEncoder());
        payloadCodecFactory.register(new VoteResponseDecoder());
        payloadCodecFactory.register(new VoteResponseEncoder());
        payloadCodecFactory.register(new TimeoutNowRequestDecoder());
        payloadCodecFactory.register(new TimeoutNowRequestEncoder());
        payloadCodecFactory.register(new TimeoutNowResponseDecoder());
        payloadCodecFactory.register(new TimeoutNowResponseEncoder());
        payloadCodecFactory.register(new AppendEntriesRequestDecoder());
        payloadCodecFactory.register(new AppendEntriesRequestEncoder());
        payloadCodecFactory.register(new AppendEntriesResponseDecoder());
        payloadCodecFactory.register(new AppendEntriesResponseEncoder());

        // index manage command codec
        payloadCodecFactory.register(new IndexQueryRequestDecoder());
        payloadCodecFactory.register(new IndexQueryRequestEncoder());
        payloadCodecFactory.register(new IndexQueryResponseDecoder());
        payloadCodecFactory.register(new IndexQueryResponseEncoder());
        payloadCodecFactory.register(new IndexStoreRequestDecoder());
        payloadCodecFactory.register(new IndexStoreRequestEncoder());
        payloadCodecFactory.register(new IndexStoreResponseDecoder());
        payloadCodecFactory.register(new IndexStoreResponseEncoder());

        // replication related command
        payloadCodecFactory.register(new ReplicateConsumePosRequestDecoder());
        payloadCodecFactory.register(new ReplicateConsumePosRequestEncoder());
        payloadCodecFactory.register(new ReplicateConsumePosResponseDecoder());
        payloadCodecFactory.register(new ReplicateConsumePosResponseEncoder());
        //nsr
        payloadCodecFactory.register(new OperatePartitionGroupCodec());

        // transaction
        payloadCodecFactory.register(new TransactionCommitRequestCodec());
        payloadCodecFactory.register(new TransactionRollbackRequestCodec());

        return payloadCodecFactory;
    }
}