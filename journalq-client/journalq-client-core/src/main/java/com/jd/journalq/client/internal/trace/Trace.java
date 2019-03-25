package com.jd.journalq.client.internal.trace;

import com.jd.laf.extension.Type;

/**
 * Trace
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/3
 */
public interface Trace extends Type<String> {

    TraceCaller begin(TraceContext context);
}