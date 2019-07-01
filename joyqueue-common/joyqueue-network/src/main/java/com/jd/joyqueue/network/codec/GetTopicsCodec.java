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
package com.jd.joyqueue.network.codec;

import com.jd.joyqueue.network.command.CommandType;
import com.jd.joyqueue.network.transport.codec.PayloadCodec;
import com.jd.joyqueue.network.transport.command.Header;
import com.jd.joyqueue.network.serializer.Serializer;
import com.jd.joyqueue.network.transport.command.Type;
import com.jd.joyqueue.network.command.GetTopics;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2018/10/19
 */
public class GetTopicsCodec implements PayloadCodec<Header,GetTopics>, Type {
    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        return new GetTopics().app(Serializer.readString(buffer)).subscribeType(buffer.readInt());
    }

    @Override
    public void encode(GetTopics payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getApp(),buffer);
        buffer.writeInt(payload.getSubscribeType());
    }

    @Override
    public int type() {
        return CommandType.GET_TOPICS;
    }
}
