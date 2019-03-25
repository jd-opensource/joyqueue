package com.jd.journalq.broker.mqtt.message;

import io.netty.handler.codec.mqtt.MqttQoS;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author majun8
 */
public class MqttStoredMessage implements Serializable {
    private static final long serialVersionUID = -1;

    final MqttQoS qos;
    final byte[] payload;
    final String topic;
    private boolean retained;
    private String clientID;
    private byte[] clientAddress;
    private short partition;
    private long index;

    public MqttStoredMessage(byte[] message, MqttQoS qos, String topic) {
        this.qos = qos;
        this.payload = message;
        this.topic = topic;
    }

    public MqttQoS getQos() {
        return qos;
    }

    public byte[] getPayload() {
        return payload;
    }

    public String getTopic() {
        return topic;
    }

    public boolean isRetained() {
        return retained;
    }

    public void setRetained(boolean retained) {
        this.retained = retained;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public byte[] getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(byte[] clientAddress) {
        this.clientAddress = clientAddress;
    }

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "MqttStoredMessage{" +
                "qos=" + qos +
                ", payload=" + Arrays.toString(payload) +
                ", topic='" + topic + '\'' +
                ", retained=" + retained +
                ", clientID='" + clientID + '\'' +
                ", clientAddress=" + Arrays.toString(clientAddress) +
                ", partition=" + partition +
                ", index=" + index +
                '}';
    }
}
