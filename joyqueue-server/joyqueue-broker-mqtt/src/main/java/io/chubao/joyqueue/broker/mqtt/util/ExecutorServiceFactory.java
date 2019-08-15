package io.chubao.joyqueue.broker.mqtt.util;

import io.chubao.joyqueue.toolkit.concurrent.NamedThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author majun8
 */
public class ExecutorServiceFactory {
    public static ExecutorService createExecutorService(int poolSize, int queueSize, String name) {
        return new ThreadPoolExecutor(
                poolSize,
                poolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueSize),
                new NamedThreadFactory(name),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
