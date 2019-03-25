package com.jd.journalq.broker.kafka.command;

import com.jd.journalq.network.transport.command.Direction;
import com.jd.journalq.network.transport.command.Payload;
import com.jd.journalq.network.transport.command.Type;

/**
 * Created by zhuduohui on 2018/8/30.
 */
public abstract class KafkaRequestOrResponse implements Payload, Type {

    // common field
    private short version;
    private int correlationId;
    private String clientId;

    private Direction direction;
    private int throttleTimeMs;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public int getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(int correlationId) {
        this.correlationId = correlationId;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setThrottleTimeMs(int throttleTimeMs) {
        this.throttleTimeMs = throttleTimeMs;
    }

    public int getThrottleTimeMs() {
        return throttleTimeMs;
    }
}

