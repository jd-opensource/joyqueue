package io.chubao.joyqueue.client.internal.trace.support;

import io.chubao.joyqueue.client.internal.trace.TraceCaller;

import java.util.List;

/**
 * CompositeTraceCaller
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/3
 */
public class CompositeTraceCaller implements TraceCaller {

    private List<TraceCaller> callers;

    public CompositeTraceCaller(List<TraceCaller> callers) {
        this.callers = callers;
    }

    @Override
    public void end() {
        for (TraceCaller caller : callers) {
            caller.end();
        }
    }

    @Override
    public void error() {
        for (TraceCaller caller : callers) {
            caller.error();
        }
    }
}