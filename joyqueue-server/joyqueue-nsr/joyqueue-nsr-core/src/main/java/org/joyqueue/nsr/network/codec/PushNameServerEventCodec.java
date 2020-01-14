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
package org.joyqueue.nsr.network.codec;

import com.alibaba.fastjson.JSON;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.event.NameServerEvent;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.network.NsrPayloadCodec;
import org.joyqueue.nsr.network.command.NsrCommandType;
import org.joyqueue.nsr.network.command.PushNameServerEvent;
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

        // TODO 临时兼容逻辑，后续去掉
        if (typeName.startsWith("com.jd.journalq")) {
            typeName = typeName.replace("com.jd.journalq", "org.joyqueue");
        }

        return new PushNameServerEvent().event(new NameServerEvent((MetaEvent)JSON.parseObject(eventValue, Class.forName(typeName)),brokerId));
    }

    @Override
    public void encode(PushNameServerEvent payload, ByteBuf buffer) throws Exception {
        if (payload.getHeader().getVersion() >= JoyQueueHeader.VERSION_V3) {
            NameServerEvent event = payload.getEvent();
            buffer.writeInt(event.getBrokerId());
            Serializer.write(JSON.toJSONString(event.getMetaEvent()),buffer,Serializer.SHORT_SIZE);
            Serializer.write(event.getMetaEvent().getTypeName(),buffer);
        } else if (payload.getHeader().getVersion() >= JoyQueueHeader.VERSION_V1) {
            // TODO 临时兼容逻辑，后续去掉
            NameServerEvent event = payload.getEvent();
            buffer.writeInt(event.getBrokerId());
            Serializer.write(JSON.toJSONString(event.getMetaEvent()),buffer,Serializer.SHORT_SIZE);
            Serializer.write(event.getMetaEvent().getTypeName().replace("org.joyqueue", "com.jd.journalq"), buffer);
        }
    }

    @Override
    public int type() {
        return NsrCommandType.PUSH_NAMESERVER_EVENT;
    }
}
