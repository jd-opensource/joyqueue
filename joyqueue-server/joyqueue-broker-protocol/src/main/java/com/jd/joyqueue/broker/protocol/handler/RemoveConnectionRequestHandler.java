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
package com.jd.joyqueue.broker.protocol.handler;

import com.jd.joyqueue.broker.BrokerContext;
import com.jd.joyqueue.broker.BrokerContextAware;
import com.jd.joyqueue.broker.protocol.JournalqCommandHandler;
import com.jd.joyqueue.broker.helper.SessionHelper;
import com.jd.joyqueue.broker.monitor.SessionManager;
import com.jd.joyqueue.exception.JournalqCode;
import com.jd.joyqueue.network.command.BooleanAck;
import com.jd.joyqueue.network.command.JournalqCommandType;
import com.jd.joyqueue.network.command.RemoveConnectionRequest;
import com.jd.joyqueue.network.session.Connection;
import com.jd.joyqueue.network.transport.Transport;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.network.transport.command.Type;

/**
 * RemoveConnectionRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/30
 */
public class RemoveConnectionRequestHandler implements JournalqCommandHandler, Type, BrokerContextAware {

    private SessionManager sessionManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.sessionManager = brokerContext.getSessionManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        RemoveConnectionRequest removeConnectionRequest = (RemoveConnectionRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null) {
            return BooleanAck.build(JournalqCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        sessionManager.removeConnection(connection.getId());
        return BooleanAck.build();
    }

    @Override
    public int type() {
        return JournalqCommandType.REMOVE_CONNECTION_REQUEST.getCode();
    }
}