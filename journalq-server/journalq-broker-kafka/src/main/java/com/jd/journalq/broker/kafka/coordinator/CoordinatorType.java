package com.jd.journalq.broker.kafka.coordinator;

/**
 * CoordinatorType
 *
 * @author luoruiheng
 * @since 1/9/18
 */
public enum CoordinatorType {

    GROUP((byte) 0),

    TRANSACTION((byte) 1),

    ;

    private byte code;

    CoordinatorType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static CoordinatorType valueOf(byte code) {
        switch (code) {
            case 0:
                return GROUP;
            case 1:
                return TRANSACTION;
            default:
                throw new IllegalArgumentException("unknown coordinator type received: " + code);
        }
    }
}