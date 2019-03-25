package com.jd.journalq.broker.kafka.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by zhangkepeng on 16-7-29.
 */
public class KafkaBroker {

    public static final KafkaBroker INVALID = new KafkaBroker(-1, StringUtils.EMPTY, -1);

    private int id;
    private String host;
    private int port;

    public KafkaBroker() {}

    public KafkaBroker(int id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        KafkaBroker kafkaBroker = (KafkaBroker)object;

        if (id != kafkaBroker.id) {
            return false;
        }
        if (port != kafkaBroker.port) {
            return false;
        }
        if (host != null ? !host.equals(kafkaBroker.host) : kafkaBroker.host != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + id;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return String.format("[id:%d,host:%s,port:%d]", id, host, port);
    }
}
