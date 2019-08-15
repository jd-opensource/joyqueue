package io.chubao.joyqueue.client.internal.consumer.interceptor;

import io.chubao.joyqueue.client.internal.Plugins;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * GlobalConsumerInterceptorManager
 *
 * author: gaohaoxiang
 * date: 2019/1/11
 */
public class GlobalConsumerInterceptorManager {

    private static final List<ConsumerInterceptor> interceptors;

    static {
        interceptors = loadInterceptors();
    }

    public static List<ConsumerInterceptor> getInterceptors() {
        return interceptors;
    }


    protected static List<ConsumerInterceptor> loadInterceptors() {
        Iterable<ConsumerInterceptor> iterable = Plugins.CONSUMER_INTERCEPTOR.extensions();
        if (iterable != null) {
            return Arrays.asList(StreamSupport.stream(iterable.spliterator(), false).toArray(ConsumerInterceptor[]::new));
        }
        return Collections.emptyList();
    }
}