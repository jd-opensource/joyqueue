/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.nsr.network.handler;

import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Types;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.nsr.network.NsrCommandHandler;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.chubao.joyqueue.nsr.network.command.PushNameServerEvent;
import io.chubao.joyqueue.nsr.network.command.PushNameServerEventAck;

/**
 * @author wylixiaobin
 * Date: 2019/2/17
 */
public class PushNameServerEventHandler implements NsrCommandHandler, Types,com.jd.laf.extension.Type<String> {
    private NameService nameService;

    @Override
    public String type() {
        return THIN_TYPE;
    }

    @Override
    public Command handle(Transport transport, Command command) {
        nameService.addEvent(((PushNameServerEvent)command.getPayload()).getEvent());
        return new Command(new PushNameServerEventAck());
    }

    @Override
    public int[] types() {
        return new int[]{NsrCommandType.PUSH_NAMESERVER_EVENT};
    }

    @Override
    public void setNameService(NameService nameService) {
        this.nameService = nameService;
    }
}
