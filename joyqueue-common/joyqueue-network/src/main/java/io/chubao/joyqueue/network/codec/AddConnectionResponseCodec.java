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
package io.chubao.joyqueue.network.codec;

import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.command.AddConnectionResponse;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * AddConnectionResponseCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/29
 */
public class AddConnectionResponseCodec implements PayloadCodec<JoyQueueHeader, AddConnectionResponse>, Type {

    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        AddConnectionResponse addConnectionResponse = new AddConnectionResponse();
        addConnectionResponse.setConnectionId(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        addConnectionResponse.setNotification(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return addConnectionResponse;
    }

    @Override
    public void encode(AddConnectionResponse payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getConnectionId(), buffer, Serializer.BYTE_SIZE);
        Serializer.write(payload.getNotification(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JoyQueueCommandType.ADD_CONNECTION_RESPONSE.getCode();
    }
}