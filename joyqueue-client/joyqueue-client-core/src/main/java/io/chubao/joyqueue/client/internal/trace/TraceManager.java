package io.chubao.joyqueue.client.internal.trace;

import io.chubao.joyqueue.client.internal.Plugins;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * TraceManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/3
 */
public class TraceManager {

    private static List<Trace> traces;

    static {
        traces = loadTraces();
    }

    public static Trace getTrace(String type) {
        return Plugins.TRACE.get(type);
    }

    public static List<Trace> getTraces() {
        return traces;
    }

    protected static List<Trace> loadTraces() {
        Iterable<Trace> iterable = Plugins.TRACE.extensions();
        if (iterable != null) {
            return Arrays.asList(StreamSupport.stream(iterable.spliterator(), false).toArray(Trace[]::new));
        }
        return Collections.emptyList();
    }


}