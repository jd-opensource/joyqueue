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
