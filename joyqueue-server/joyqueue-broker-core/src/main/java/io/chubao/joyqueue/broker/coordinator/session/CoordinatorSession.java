package io.chubao.joyqueue.broker.coordinator.session;

import io.chubao.joyqueue.broker.coordinator.config.CoordinatorConfig;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.CommandCallback;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.chubao.joyqueue.toolkit.service.Service;

/**
 * CoordinatorSession
 *
 * author: gaohaoxiang
 * date: 2019/4/12
 */
public class CoordinatorSession extends Service {

    private int brokerId;
    private String brokerHost;
    private int brokerPort;
    private CoordinatorConfig config;
    private Transport transport;

    public CoordinatorSession() {

    }

    public CoordinatorSession(int brokerId, String brokerHost, int brokerPort, CoordinatorConfig config, Transport transport) {
        this.brokerId = brokerId;
        this.brokerHost = brokerHost;
        this.brokerPort = brokerPort;
        this.config = config;
        this.transport = transport;
    }

    @Override
    protected void doStop() {
        transport.stop();
    }

    public void oneway(Command command) throws TransportException {
        oneway(command, config.getSessionTimeout());
    }

    public void oneway(Command command, long timeout) throws TransportException {
        transport.oneway(command, timeout);
    }

    public Command sync(Command command) throws TransportException {
        return sync(command, config.getSessionTimeout());
    }

    public Command sync(Command command, int timeout) throws TransportException {
        return transport.sync(command, timeout);
    }

    public void async(Command command, CommandCallback callback) throws TransportException {
        async(command, config.getSessionTimeout(), callback);
    }

    void async(Command command, long timeout, CommandCallback callback) throws TransportException {
        transport.async(command, timeout, callback);
    }

    public int getBrokerId() {
        return brokerId;
    }

    public String getBrokerHost() {
        return brokerHost;
    }

    public int getBrokerPort() {
        return brokerPort;
    }
}