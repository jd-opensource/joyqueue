package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetBrokerByRetryTypeAck;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetBrokerByRetryTypeAckCodec implements NsrPayloadCodec<GetBrokerByRetryTypeAck>, Type {
    @Override
    public GetBrokerByRetryTypeAck decode(Header header, ByteBuf buffer) throws Exception {
        GetBrokerByRetryTypeAck allBrokersAck = new GetBrokerByRetryTypeAck();
            int brokerSize = buffer.readInt();
            List<Broker> list = new ArrayList<>(brokerSize);
            for(int i =0;i<brokerSize;i++){
                list.add(Serializer.readBroker(buffer));
            }
            allBrokersAck.brokers(list);
        return allBrokersAck;
    }

    @Override
    public void encode(GetBrokerByRetryTypeAck payload, ByteBuf buffer) throws Exception {
        List<Broker> brokerList = payload.getBrokers();
        if(null==brokerList||brokerList.size()<1){
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(brokerList.size());
        for(Broker broker : brokerList){
            Serializer.write(broker,buffer);
        }
    }

    @Override
    public int type() {
        return NsrCommandType.GET_BROKER_BY_RETRYTYPE_ACK;
    }
}
