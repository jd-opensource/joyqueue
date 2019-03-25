package com.jd.journalq.client.internal.producer.domain;

/**
 * TransactionStatus
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public enum TransactionStatus {

    UNKNOWN,

    PREPARE,

    COMMITTED,

    ROLLBACK,

    ;
}