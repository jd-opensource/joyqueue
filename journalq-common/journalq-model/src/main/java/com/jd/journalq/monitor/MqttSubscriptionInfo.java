package com.jd.journalq.monitor;

/**
 * @author majun8
 */
public class MqttSubscriptionInfo extends BaseMonitorInfo {

    private int qos;
    private String topic;
    private String clientId;
    private int size;

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public int hashCode() {
        int result = clientId != null ? clientId.hashCode() : 0;
        result = 31 * result + (topic != null ? topic.hashCode() : 0);
        return result;
    }
}
