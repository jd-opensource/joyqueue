package io.chubao.joyqueue.message;


/**
 * 日志接口
 */
public interface JoyQueueLog {
    //日志类型
    byte TYPE_MESSAGE = (byte) 6;
    byte TYPE_TX_PREPARE = (byte) 3;
    byte TYPE_TX_COMMIT = (byte) 4;
    byte TYPE_TX_ROLLBACK = (byte) 5;

    int getStoreTime();

    byte getType();

    void setStoreTime(int storeTime);

    int getSize();

    String getTxId();

    long getStartTime();

}
