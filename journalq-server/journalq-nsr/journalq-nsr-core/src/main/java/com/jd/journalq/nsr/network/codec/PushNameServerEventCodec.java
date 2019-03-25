package com.jd.journalq.nsr.network.codec;

import com.alibaba.fastjson.JSON;
import com.jd.journalq.event.MetaEvent;
import com.jd.journalq.event.NameServerEvent;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import com.jd.journalq.nsr.network.command.PushNameServerEvent;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class PushNameServerEventCodec implements NsrPayloadCodec<PushNameServerEvent>, Type {
    @Override
    public PushNameServerEvent decode(Header header, ByteBuf buffer) throws Exception {
        int brokerId = buffer.readInt();
        String eventValue = Serializer.readString(buffer,Serializer.SHORT_SIZE);
        String typeName = Serializer.readString(buffer);
        return new PushNameServerEvent().event(new NameServerEvent((MetaEvent)JSON.parseObject(eventValue, Class.forName(typeName)),brokerId));
    }

    @Override
    public void encode(PushNameServerEvent payload, ByteBuf buffer) throws Exception {
        NameServerEvent event = payload.getEvent();
        buffer.writeInt(event.getBrokerId());
        Serializer.write(JSON.toJSONString(event.getMetaEvent()),buffer,Serializer.SHORT_SIZE);
        Serializer.write(event.getMetaEvent().getClass().getTypeName(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.PUSH_NAMESERVER_EVENT;
    }
}
