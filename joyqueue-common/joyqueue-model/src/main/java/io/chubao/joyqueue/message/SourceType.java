package io.chubao.joyqueue.message;


public enum SourceType {

    JMQ((byte) 0),

    KAFKA((byte) 1),

    MQTT((byte) 2),

    JOYQUEUE((byte) 3),

    OTHERS((byte) 10),

    INTERNAL((byte) 11),

    ;

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
