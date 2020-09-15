package org.joyqueue.broker.joyqueue0.command;

import java.io.Serializable;

/**
 * Created by zhuduohui on 2018/1/16.
 */
public class Collector implements Serializable {
    private static final long serialVersionUID = -6448500612587293222L;

    String address;
    int port;

    public Collector() {}

    public Collector(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{\"address\":").append(address)
                .append(",\"port\":\"").append(port)
                .append("}");
        return sb.toString();
    }
}

