package io.chubao.joyqueue.nsr;

/**
 * InternalServiceProvider
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public interface InternalServiceProvider {

    <T> T getService(Class<T> type);
}