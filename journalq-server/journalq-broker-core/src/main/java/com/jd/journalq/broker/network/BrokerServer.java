package com.jd.journalq.broker.network;

import com.jd.journalq.broker.network.frontend.FrontendServer;
import com.jd.journalq.broker.network.listener.BrokerTransportListener;
import com.jd.journalq.broker.network.protocol.ProtocolManager;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.monitor.SessionManager;
import com.jd.journalq.network.transport.config.ServerConfig;
import com.jd.journalq.broker.network.backend.BackendServer;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.service.Service;

/**
 * broker服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/14
 */
public class BrokerServer extends Service {

    private FrontendServer frontendServer;
    private BackendServer backendServer;
    private BrokerTransportListener transportListener;

    public BrokerServer(BrokerContext brokerContext, ProtocolManager protocolManager) {
        Preconditions.checkArgument(brokerContext != null, "broker context can not be null");
        Preconditions.checkArgument(protocolManager != null, "protocol manager can not be null");

        ServerConfig frontendConfig = brokerContext.getBrokerConfig().getFrontendConfig();
        ServerConfig backendConfig = brokerContext.getBrokerConfig().getBackendConfig();
        SessionManager sessionManager = brokerContext.getSessionManager();

        this.transportListener = new BrokerTransportListener(sessionManager);
        this.frontendServer = new FrontendServer(frontendConfig, protocolManager);
        this.backendServer = new BackendServer(backendConfig, brokerContext);
        this.frontendServer.addListener(transportListener);
        this.backendServer.addListener(transportListener);
    }

    @Override
    protected void doStart() throws Exception {
        this.frontendServer.start();
        this.backendServer.start();
    }

    @Override
    protected void doStop() {
        this.frontendServer.removeListener(transportListener);
        this.backendServer.removeListener(transportListener);
        this.frontendServer.stop();
        this.backendServer.stop();
    }
}