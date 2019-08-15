package io.chubao.joyqueue.client.internal.producer.domain;

/**
 * TransactionStatus
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public enum TransactionStatus {

    UNKNOWN,

    PREPARE,

    COMMITTED,

    ROLLBACK,

    ;
}