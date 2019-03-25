package com.jd.journalq.client.internal.producer.interceptor;

import com.jd.journalq.client.internal.Plugins;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * GlobalProducerInterceptorManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/11
 */
public class GlobalProducerInterceptorManager {

    private static final List<ProducerInterceptor> interceptors;

    static {
        interceptors = loadInterceptors();
    }

    public static List<ProducerInterceptor> getInterceptors() {
        return interceptors;
    }

    protected static List<ProducerInterceptor> loadInterceptors() {
        Iterable<ProducerInterceptor> iterable = Plugins.PRODUCER_INTERCEPTOR.extensions();
        if (iterable != null) {
            return Arrays.asList(StreamSupport.stream(iterable.spliterator(), false).toArray(ProducerInterceptor[]::new));
        }
        return Collections.emptyList();
    }
}