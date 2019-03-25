package com.jd.journalq.network.domain;

import java.io.Serializable;

/**
 * BrokerNode
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/29
 */
public class BrokerNode implements Serializable {

    private int id;
    private String host;
    private int port;
    private String dataCenter;
    private boolean nearby;
    private int weight;

    public BrokerNode() {
    }

    public BrokerNode(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public BrokerNode(int id, String host, int port, String dataCenter, boolean nearby, int weight) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.dataCenter = dataCenter;
        this.nearby = nearby;
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(String dataCenter) {
        this.dataCenter = dataCenter;
    }

    public void setNearby(boolean nearby) {
        this.nearby = nearby;
    }

    public boolean isNearby() {
        return nearby;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrokerNode that = (BrokerNode) o;
        return id == that.getId();
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "BrokerNode{" +
                "id=" + id +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", dataCenter='" + dataCenter + '\'' +
                ", nearby=" + nearby +
                ", weight=" + weight +
                '}';
    }
}