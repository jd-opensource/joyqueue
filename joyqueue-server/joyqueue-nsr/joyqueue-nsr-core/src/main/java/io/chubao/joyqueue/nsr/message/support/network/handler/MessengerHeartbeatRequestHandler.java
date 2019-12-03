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
package io.chubao.joyqueue.nsr.message.support.network.handler;

import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandler;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;

/**
 * MessengerHeartbeatRequestHandler
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerHeartbeatRequestHandler implements CommandHandler, Type {

    @Override
    public Command handle(Transport transport, Command command) {
        return BooleanAck.build();
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_MESSENGER_HEARTBEAT_REQUEST;
    }
}