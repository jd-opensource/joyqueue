package io.chubao.joyqueue.nsr.message.support.session;

import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.CommandCallback;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.chubao.joyqueue.nsr.config.MessengerConfig;
import io.chubao.joyqueue.toolkit.service.Service;

/**
 * MessengerSession
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerSession extends Service {

    private int brokerId;
    private String brokerHost;
    private int brokerPort;
    private MessengerConfig config;
    private Transport transport;

    public MessengerSession() {

    }

    public MessengerSession(int brokerId, String brokerHost, int brokerPort, MessengerConfig config, Transport transport) {
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

    public void async(Command command, long timeout, CommandCallback callback) throws TransportException {
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

    @Override
    public String toString() {
        return "MessengerSession{" +
                "brokerId=" + brokerId +
                ", brokerHost='" + brokerHost + '\'' +
                ", brokerPort=" + brokerPort +
                '}';
    }
}