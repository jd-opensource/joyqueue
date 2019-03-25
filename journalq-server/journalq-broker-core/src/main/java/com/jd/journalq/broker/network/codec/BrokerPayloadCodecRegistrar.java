package com.jd.journalq.broker.network.codec;

import com.jd.journalq.broker.election.network.codec.*;
import com.jd.journalq.broker.index.network.codec.*;
import com.jd.journalq.network.codec.BooleanAckCodec;
import com.jd.journalq.network.transport.codec.PayloadCodecFactory;
import com.jd.journalq.nsr.network.codec.OperatePartitionGroupCodec;

/**
 * jmq消息体编解码器注册器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
// 使用BrokerPayloadCodec接口通过spi方式注册
@Deprecated
public class BrokerPayloadCodecRegistrar {

    public static PayloadCodecFactory register(PayloadCodecFactory payloadCodecFactory) {
        // boolean ack command codec
        payloadCodecFactory.register(new BooleanAckCodec());

        // retry message command codec
    /*    payloadCodecFactory.register(new GetRetryCodec());
        payloadCodecFactory.register(new GetRetryAckCodec());
        payloadCodecFactory.register(new GetRetryCountCodec());
        payloadCodecFactory.register(new GetRetryCountAckCodec());
        payloadCodecFactory.register(new PutRetryCodec());
        payloadCodecFactory.register(new UpdateRetryCodec());*/

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
        return payloadCodecFactory;
    }
}