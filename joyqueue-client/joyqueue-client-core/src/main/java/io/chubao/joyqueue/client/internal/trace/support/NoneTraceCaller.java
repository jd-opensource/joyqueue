package io.chubao.joyqueue.client.internal.trace.support;

import io.chubao.joyqueue.client.internal.trace.TraceCaller;

/**
 * NoneTraceCaller
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/3
 */
public class NoneTraceCaller implements TraceCaller {

    private static final NoneTraceCaller INSTANCE = new NoneTraceCaller();

    public static NoneTraceCaller getInstance() {
        return INSTANCE;
    }

    @Override
    public void end() {

    }

    @Override
    public void error() {

    }
}