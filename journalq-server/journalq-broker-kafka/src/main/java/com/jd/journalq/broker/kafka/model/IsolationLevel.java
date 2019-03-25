package com.jd.journalq.broker.kafka.model;

/**
 * IsolationLevel
 *
 * @author luoruiheng
 * @since 1/9/18
 */
public enum IsolationLevel {

    READ_UNCOMMITTED((byte) 0),
    READ_COMMITTED((byte) 1);

    private final byte id;

    IsolationLevel(byte id) {
        this.id = id;
    }

    public byte id() {
        return id;
    }

    public static IsolationLevel valueOf(byte id) {
        switch (id) {
            case 0:
                return READ_UNCOMMITTED;
            case 1:
                return READ_COMMITTED;
            default:
                throw new IllegalArgumentException("Unknown isolation level " + id);
        }
    }

}