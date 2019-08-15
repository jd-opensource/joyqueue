package io.chubao.joyqueue.client.internal.trace.support;

import io.chubao.joyqueue.client.internal.trace.Trace;
import io.chubao.joyqueue.client.internal.trace.TraceCaller;
import io.chubao.joyqueue.client.internal.trace.TraceContext;

/**
 * NoneTrace
 *
 * author: gaohaoxiang
 * date: 2019/1/3
 */
public class NoneTrace implements Trace {

    private static final NoneTrace INSTANCE = new NoneTrace();

    public static NoneTrace getInstance() {
        return INSTANCE;
    }

    @Override
    public TraceCaller begin(TraceContext context) {
        return NoneTraceCaller.getInstance();
    }

    @Override
    public String type() {
        return "none";
    }
}