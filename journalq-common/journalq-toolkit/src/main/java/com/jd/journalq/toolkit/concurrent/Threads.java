package com.jd.journalq.toolkit.concurrent;

import com.jd.journalq.toolkit.exception.Abnormity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 线程工具类
 * Created by hexiaofeng on 15-3-12.
 */
public class Threads {

    /**
     * 调用任务
     *
     * @param tasks      任务
     * @param maxThreads 最大线程数
     * @param abnormity  异常回调
     * @return 任务结果
     * @throws InterruptedException
     */
    public static <T, M extends Callable<T>> List<T> invoke(final List<M> tasks, final int maxThreads,
            final Abnormity abnormity) throws InterruptedException {
        return invoke(tasks, maxThreads, abnormity, null);
    }

    /**
     * 调用任务
     *
     * @param tasks      任务
     * @param maxThreads 最大线程数
     * @param abnormity  异常回调
     * @param name       线程名称
     * @return 任务结果
     * @throws InterruptedException
     */
    public static <T, M extends Callable<T>> List<T> invoke(final List<M> tasks, final int maxThreads,
            final Abnormity abnormity, final String name) throws InterruptedException {
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<T>();
        }
        if (maxThreads < 0) {
            throw new IllegalArgumentException("maxThreads must be greater than 0");
        }
        // 请求数量
        int threads = Math.min(maxThreads, tasks.size());
        // 构造线程池
        ExecutorService executorService;
        if (name != null && !name.isEmpty()) {
            executorService = Executors.newFixedThreadPool(threads, new NamedThreadFactory(name));
        } else {
            executorService = Executors.newFixedThreadPool(threads);
        }
        try {
            // 并发调用
            return invoke(tasks, executorService, abnormity);
        } finally {
            executorService.shutdownNow();
        }
    }

    /**
     * 调用任务
     *
     * @param tasks           任务
     * @param executorService 线程池
     * @param abnormity       异常回调
     * @return 任务结果
     * @throws InterruptedException
     */
    public static <T, M extends Callable<T>> List<T> invoke(final List<M> tasks, final ExecutorService executorService,
            final Abnormity abnormity) throws InterruptedException {
        List<T> results = new ArrayList<T>();
        if (tasks == null || tasks.isEmpty()) {
            return results;
        }
        if (executorService == null) {
            throw new IllegalArgumentException("executorService can not be null");
        }

        // 并发调用
        List<Future<T>> futures = executorService.invokeAll(tasks);
        Throwable exception;
        for (Future<T> future : futures) {
            try {
                T ret = future.get();
                if (ret != null) {
                    results.add(ret);
                }
            } catch (InterruptedException e) {
                // 终止了
                throw e;
            } catch (ExecutionException e) {
                // 执行出错
                exception = e.getCause();
                if (abnormity != null) {
                    if (exception != null) {
                        abnormity.onException(exception);
                    } else {
                        abnormity.onException(e);
                    }
                }
            }
        }
        return results;
    }

}
