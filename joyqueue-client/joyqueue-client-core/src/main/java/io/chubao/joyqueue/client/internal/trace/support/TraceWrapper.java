package io.chubao.joyqueue.client.internal.trace.support;

import io.chubao.joyqueue.client.internal.trace.Trace;
import io.chubao.joyqueue.client.internal.trace.TraceCaller;
import io.chubao.joyqueue.client.internal.trace.TraceContext;
import io.chubao.joyqueue.client.internal.trace.TraceManager;

import java.util.List;

/**
 * TraceWrapper
 *
 * author: gaohaoxiang
 * date: 2019/1/3
 */
public class TraceWrapper implements Trace {

    private static final TraceWrapper INSTANCE = new TraceWrapper();
    private static final CompositeTrace COMPOSITE_TRACE;

    static {
        List<Trace> traces = TraceManager.getTraces();
        COMPOSITE_TRACE = new CompositeTrace(traces);
    }

    public static TraceWrapper getInstance() {
        return INSTANCE;
    }

    @Override
    public TraceCaller begin(TraceContext context) {
        if (!context.getType().isEnable()) {
            return NoneTraceCaller.getInstance();
        }
        if (COMPOSITE_TRACE.getTraces().isEmpty()) {
            return NoneTraceCaller.getInstance();
        }
        return COMPOSITE_TRACE.begin(context);
    }

    @Override
    public String type() {
        return "wrapper";
    }
}