package com.jd.journalq.model.domain;

public class MqttProxyClient {
    private String clientId;
    private boolean status;

    public MqttProxyClient(String clientId,boolean status){
        this.clientId=clientId;
        this.status=status;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }



    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
