/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.client.internal.trace.support;

import org.joyqueue.client.internal.trace.Trace;
import org.joyqueue.client.internal.trace.TraceCaller;
import org.joyqueue.client.internal.trace.TraceContext;
import org.joyqueue.client.internal.trace.TraceManager;

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