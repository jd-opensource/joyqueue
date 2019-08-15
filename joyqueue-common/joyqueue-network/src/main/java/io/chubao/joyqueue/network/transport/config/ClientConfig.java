package io.chubao.joyqueue.network.transport.config;

/**
 * ClientConfig
 *
 * author: gaohaoxiang
 * date: 2018/8/13
 */
public class ClientConfig extends TransportConfig {

    private boolean preferIPv6 = false;
    // 连接超时(毫秒)
    private int connectionTimeout = 5 * 1000;

    public ClientConfig() {
    }

    public boolean getPreferIPv6() {
        return preferIPv6;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setPreferIPv6(boolean preferIPv6) {
        this.preferIPv6 = preferIPv6;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
}