package io.chubao.joyqueue.nsr;

import com.jd.laf.extension.Type;

/**
 * InternalServiceProvider
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public interface InternalServiceProvider extends Type<String> {

    <T> T getService(Class<T> type);
}