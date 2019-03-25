package com.jd.journalq.common.network.transport.config;


/**
 * 通信服务配置
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public class ServerConfig extends TransportConfig {
    public static final int DEFAULT_TRANSPORT_PORT = 50088;
    private int port = DEFAULT_TRANSPORT_PORT;

    public ServerConfig() {
    }


    public void setPort(int port) {
        this.port = port;
    }


    public int getPort() {
        return port;
    }

    @Override
    public int getAcceptThread() {
        return super.getAcceptThread();
    }

    @Override
    public void setAcceptThread(int acceptThread) {
        super.setAcceptThread(acceptThread);
    }

    @Override
    public int getIoThread() {
        return super.getIoThread();
    }

    @Override
    public void setIoThread(int ioThread) {
        super.setIoThread(ioThread);
    }
}