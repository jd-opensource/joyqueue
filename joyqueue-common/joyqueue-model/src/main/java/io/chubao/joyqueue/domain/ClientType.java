package io.chubao.joyqueue.domain;

public enum ClientType {
    JOYQUEUE((byte) 0, "joyqueue"),

    KAFKA((byte) 1, "kafka"),

    MQTT((byte) 2, "mqtt"),

    OTHERS((byte) 10, "others");

    private byte value;
    private String name;

    ClientType(byte value, String name) {
        this.value = value;
        this.name = name;
    }

    public byte value() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static ClientType valueOf(int value) {
        for (ClientType type : ClientType.values()) {
            if (value == type.value) {
                return type;
            }
        }
        return OTHERS;
    }
}
