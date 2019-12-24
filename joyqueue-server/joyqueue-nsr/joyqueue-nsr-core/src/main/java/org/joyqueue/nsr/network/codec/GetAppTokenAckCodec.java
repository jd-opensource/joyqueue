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

import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.network.NsrPayloadCodec;
import org.joyqueue.nsr.network.command.GetAppTokenAck;
import org.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/2/13
 */
public class GetAppTokenAckCodec implements NsrPayloadCodec<GetAppTokenAck>, Type {
    @Override
    public GetAppTokenAck decode(Header header, ByteBuf buffer) throws Exception {
        GetAppTokenAck appTokenAck = new GetAppTokenAck();
        if(buffer.readBoolean()){
            appTokenAck.appToken(Serializer.readAppToken(buffer));
        }
        return appTokenAck;
    }

    @Override
    public void encode(GetAppTokenAck payload, ByteBuf buffer) throws Exception {
        if(null==payload.getAppToken()){
            buffer.writeBoolean(false);
            return;
        }
        buffer.writeBoolean(true);
        Serializer.write(payload.getAppToken(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_APP_TOKEN_ACK;
    }
}
