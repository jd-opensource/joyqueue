package com.jd.journalq.common.monitor;

/**
 * @author majun8
 */
public class MqttThreadClientInfo extends BaseMonitorInfo {
    private String clientID;
    private boolean debug;

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
