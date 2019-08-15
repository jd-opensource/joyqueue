package io.chubao.joyqueue.client.internal.transport;

import io.chubao.joyqueue.client.internal.transport.config.TransportConfig;
import io.chubao.joyqueue.network.transport.TransportState;
import io.chubao.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClientHeartbeatThread
 *
 * author: gaohaoxiang
 * date: 2019/1/7
 */
public class ClientHeartbeatThread implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(ClientHeartbeatThread.class);

    private TransportConfig transportConfig;
    private ClientGroupManager clientGroupManager;

    public ClientHeartbeatThread(TransportConfig transportConfig, ClientGroupManager clientGroupManager) {
        this.transportConfig = transportConfig;
        this.clientGroupManager = clientGroupManager;
    }

    @Override
    public void run() {
        for (ClientGroup clientGroup : clientGroupManager.getGroups()) {
            doHeartbeat(clientGroup);
        }
    }

    protected void doHeartbeat(ClientGroup clientGroup) {
        for (Client client : clientGroup.getClients()) {
            if (client.getTransport().state().equals(TransportState.DISCONNECTED)
                    || (client.getLastUseTime() != 0 && SystemClock.now() - client.getLastUseTime() >= transportConfig.getHeartbeatMaxIdleTime())) {
                doHeartbeat(client);
            }
        }
    }

    protected void doHeartbeat(Client client) {
        try {
            client.heartbeat(transportConfig.getHeartbeatTimeout());
        } catch (Exception e) {
            logger.debug("client heartbeat exception", e);
        }
    }
}