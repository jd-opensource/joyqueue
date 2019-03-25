package com.jd.journalq.model.domain;

public enum MqttProxyThreadType implements EnumItem {

    CONSUME(1,"consume"),
    DELIVERY(2,"delivery");
    private int value;
    private String description;
    MqttProxyThreadType(int value, String description){
       this.value=value;
       this.description=description;
    }

    @Override
    public int value() {
        return value;
    }

    @Override
    public String description() {
        return description;
    }
}
