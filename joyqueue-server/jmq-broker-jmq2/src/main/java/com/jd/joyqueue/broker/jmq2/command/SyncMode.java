package com.jd.joyqueue.broker.jmq2.command;

/**
 * 复制方式
 */
public enum SyncMode {

    /**
     * 异步复制
     */
    ASYNCHRONOUS,
    /**
     * 同步复制
     */
    SYNCHRONOUS;


    public static SyncMode valueOf(int value) {
        switch (value) {
            case 0:
                return ASYNCHRONOUS;
            case 1:
                return SYNCHRONOUS;
            default:
                return null;
        }
    }

}
