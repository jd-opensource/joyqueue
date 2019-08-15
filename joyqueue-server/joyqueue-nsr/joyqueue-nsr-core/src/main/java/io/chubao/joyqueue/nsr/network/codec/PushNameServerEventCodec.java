package io.chubao.joyqueue.nsr.network.codec;

import com.alibaba.fastjson.JSON;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.event.NameServerEvent;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.chubao.joyqueue.nsr.network.command.PushNameServerEvent;
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
        Serializer.write(event.getMetaEvent().getTypeName(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.PUSH_NAMESERVER_EVENT;
    }
}
