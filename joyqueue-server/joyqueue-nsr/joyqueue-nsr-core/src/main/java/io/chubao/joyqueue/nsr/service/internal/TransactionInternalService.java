package io.chubao.joyqueue.nsr.service.internal;

/**
 * TransactionInternalService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public interface TransactionInternalService {

    void begin();

    void commit();

    void rollback();
}