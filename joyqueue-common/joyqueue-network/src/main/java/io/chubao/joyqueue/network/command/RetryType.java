package io.chubao.joyqueue.network.command;

/**
 * RetryType
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public enum RetryType {

    NONE(0),

    TIMEOUT(1),

    EXCEPTION(2),

    OTHER(3)

    ;

    private byte type;

    RetryType(int type) {
        this.type = (byte) type;
    }

    public byte getType() {
        return type;
    }

    public static RetryType valueOf(int type) {
        for (RetryType retryType : RetryType.values()) {
            if (retryType.getType() == type) {
                return retryType;
            }
        }
        return null;
    }
}