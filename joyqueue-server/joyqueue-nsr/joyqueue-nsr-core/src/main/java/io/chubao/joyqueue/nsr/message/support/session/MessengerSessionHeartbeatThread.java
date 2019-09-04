package io.chubao.joyqueue.nsr.message.support.session;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.network.transport.command.JoyQueueCommand;
import io.chubao.joyqueue.nsr.config.MessengerConfig;
import io.chubao.joyqueue.nsr.message.support.network.command.MessengerHeartbeatRequest;
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