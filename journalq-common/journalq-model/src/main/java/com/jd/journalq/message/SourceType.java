package com.jd.journalq.message;


public enum SourceType {

    JMQ2((byte) 0),

    KAFKA((byte) 1),

    MQTT((byte) 2),

    JMQ((byte) 3),

    OTHERS((byte) 10);

    private byte value;

    SourceType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static SourceType valueOf(byte value) {
        for (SourceType type : SourceType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return OTHERS;
    }
}
