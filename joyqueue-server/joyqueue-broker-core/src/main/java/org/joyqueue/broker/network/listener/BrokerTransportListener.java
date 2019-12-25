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
package org.joyqueue.broker.network.listener;

import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.network.event.TransportEvent;
import org.joyqueue.network.event.TransportEventType;
import org.joyqueue.network.session.Connection;
import org.joyqueue.toolkit.concurrent.EventListener;

/**
 * BrokerTransportListener
 *
 * author: gaohaoxiang
 * date: 2018/10/10
 */
public class BrokerTransportListener implements EventListener<TransportEvent> {

    private SessionManager sessionManager;

    public BrokerTransportListener(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void onEvent(TransportEvent event) {
        TransportEventType type = event.getType();
        if (!(type.equals(TransportEventType.CLOSE) || type.equals(TransportEventType.EXCEPTION))) {
            return;
        }
        Connection connection = SessionHelper.getConnection(event.getTransport());
        if (connection == null) {
            return;
        }
        sessionManager.removeConnection(connection.getId());
    }
}