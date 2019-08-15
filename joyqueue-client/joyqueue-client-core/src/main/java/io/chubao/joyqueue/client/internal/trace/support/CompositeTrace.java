package io.chubao.joyqueue.client.internal.trace.support;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.client.internal.trace.Trace;
import io.chubao.joyqueue.client.internal.trace.TraceCaller;
import io.chubao.joyqueue.client.internal.trace.TraceContext;

import java.util.List;

/**
 * CompositeTrace
 *
 * author: gaohaoxiang
 * date: 2019/1/3
 */
public class CompositeTrace implements Trace {

    private List<Trace> traces;

    public CompositeTrace(List<Trace> traces) {
        this.traces = traces;
    }

    @Override
    public TraceCaller begin(TraceContext context) {
        List<TraceCaller> callers = Lists.newLinkedList();
        for (Trace trace : traces) {
            TraceCaller caller = trace.begin(context);
            callers.add(caller);
        }
        return new CompositeTraceCaller(callers);
    }

    public List<Trace> getTraces() {
        return traces;
    }

    @Override
    public String type() {
        return "composite";
    }
}