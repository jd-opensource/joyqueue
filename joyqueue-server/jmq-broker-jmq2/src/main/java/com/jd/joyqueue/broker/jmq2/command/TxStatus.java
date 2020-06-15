package com.jd.joyqueue.broker.jmq2.command;

/**
 * 事务状态.
 *
 * @author lindeqiang
 * @since 2016/5/16 17:11
 */
public enum TxStatus {
    //事务准备
    PREPARE,
    //事务已提交
    COMMITTED,
    //事务已回滚
    ROLLBACK,
    //未知状态
    UNKNOWN, Language;

    public static TxStatus valueOf(int value) {
        switch (value) {
            case 0:
                return PREPARE;
            case 1:
                return COMMITTED;
            case 2:
                return ROLLBACK;
            case 3:
            default:
                return UNKNOWN;
        }
    }

}
