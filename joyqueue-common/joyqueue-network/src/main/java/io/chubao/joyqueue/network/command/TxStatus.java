package io.chubao.joyqueue.network.command;

/**
 * TxStatus
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public enum TxStatus {

    UNKNOWN(0),

    PREPARE(1),

    COMMITTED(2),

    ROLLBACK(3),

    ;

    private byte type;

    TxStatus(int type) {
        this.type = (byte) type;
    }

    public byte getType() {
        return type;
    }

    public static TxStatus valueOf(int type) {
        for (TxStatus status : TxStatus.values()) {
            if (status.getType() == type) {
                return status;
            }
        }
        return null;
    }
}