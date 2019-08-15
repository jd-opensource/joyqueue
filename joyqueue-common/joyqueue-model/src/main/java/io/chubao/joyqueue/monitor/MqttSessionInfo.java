package io.chubao.joyqueue.monitor;

import java.util.HashSet;
import java.util.Set;

/**
 * @author majun8
 */
public class MqttSessionInfo extends BaseMonitorInfo {

    private String clientID;
    private Set<MqttSubscriptionInfo> subscriptions = new HashSet<>();
    private boolean cleanSession;
    private int messageAcknowledgedZoneSize;
    private long consumed;

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public Set<MqttSubscriptionInfo> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<MqttSubscriptionInfo> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public int getMessageAcknowledgedZoneSize() {
        return messageAcknowledgedZoneSize;
    }

    public void setMessageAcknowledgedZoneSize(int messageAcknowledgedZoneSize) {
        this.messageAcknowledgedZoneSize = messageAcknowledgedZoneSize;
    }

    public long getConsumed() {
        return consumed;
    }

    public void setConsumed(long consumed) {
        this.consumed = consumed;
    }
}
