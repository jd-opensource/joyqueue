package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.domain.Broker;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetAllBrokersAck;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetAllBrokersAckCodec implements NsrPayloadCodec<GetAllBrokersAck>, Type {
    @Override
    public GetAllBrokersAck decode(Header header, ByteBuf buffer) throws Exception {
        GetAllBrokersAck allBrokersAck = new GetAllBrokersAck();
            int brokerSize = buffer.readInt();
            List<Broker> list = new ArrayList<>(brokerSize);
            //3. broker array
            for(int i =0;i<brokerSize;i++){
                list.add(Serializer.readBroker(buffer));
            }
            allBrokersAck.brokers(list);
        return allBrokersAck;
    }

    @Override
    public void encode(GetAllBrokersAck payload, ByteBuf buffer) throws Exception {
        List<Broker> brokerList = payload.getBrokers();
        if(null==brokerList||brokerList.size()<1){
            buffer.writeInt(0);
            return;
        }
        //2. int
        buffer.writeInt(brokerList.size());
        //3. broker list
        for(Broker broker : brokerList){
            Serializer.write(broker,buffer);
        }
    }

    @Override
    public int type() {
        return NsrCommandType.GET_ALL_BROKERS_ACK;
    }
}
