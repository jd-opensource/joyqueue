package io.chubao.joyqueue.broker.election;

import io.chubao.joyqueue.network.transport.config.ServerConfig;

/**
 * Created by zhuduohui on 2018/10/31.
 */
public class ServerConfigStub extends ServerConfig {
    private int port;

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
