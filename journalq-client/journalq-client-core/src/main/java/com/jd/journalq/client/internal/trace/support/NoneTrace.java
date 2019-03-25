package com.jd.journalq.client.internal.trace.support;

import com.jd.journalq.client.internal.trace.Trace;
import com.jd.journalq.client.internal.trace.TraceCaller;
import com.jd.journalq.client.internal.trace.TraceContext;

/**
 * NoneTrace
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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