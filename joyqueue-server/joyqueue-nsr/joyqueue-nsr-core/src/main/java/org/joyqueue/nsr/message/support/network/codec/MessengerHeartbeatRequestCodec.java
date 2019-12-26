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
package org.joyqueue.nsr.message.support.network.codec;

import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.message.support.network.command.MessengerHeartbeatRequest;
import org.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * MessengerPublishRequestCodec
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerHeartbeatRequestCodec implements PayloadCodec<JoyQueueHeader, MessengerHeartbeatRequest>, Type {

    @Override
    public MessengerHeartbeatRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        return new MessengerHeartbeatRequest();
    }

    @Override
    public void encode(MessengerHeartbeatRequest payload, ByteBuf buffer) throws Exception {
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_MESSENGER_HEARTBEAT_REQUEST;
    }
}