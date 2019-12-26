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
package org.joyqueue.nsr.message.support.session;

import com.google.common.collect.Lists;
import org.joyqueue.network.transport.command.JoyQueueCommand;
import org.joyqueue.nsr.config.MessengerConfig;
import org.joyqueue.nsr.message.support.network.command.MessengerHeartbeatRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * MessengerSessionHeartbeatThread
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class MessengerSessionHeartbeatThread implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(MessengerSessionHeartbeatThread.class);

    private MessengerConfig config;
    private MessengerSessionManager messengerSessionManager;

    public MessengerSessionHeartbeatThread(MessengerConfig config, MessengerSessionManager messengerSessionManager) {
        this.config = config;
        this.messengerSessionManager = messengerSessionManager;
    }

    @Override
    public void run() {
        ArrayList<MessengerSession> sessions = Lists.newArrayList(messengerSessionManager.getSessions().values());
        for (MessengerSession session : sessions) {
            try {
                session.sync(new JoyQueueCommand(new MessengerHeartbeatRequest()), config.getHeartbeatTimeout());
            } catch (Exception e) {
                logger.warn("heartbeat exception, session: {}", session, e);
            }
        }
    }
}