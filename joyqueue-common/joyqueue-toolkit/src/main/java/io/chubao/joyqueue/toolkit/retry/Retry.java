package io.chubao.joyqueue.toolkit.retry;

import io.chubao.joyqueue.toolkit.lang.Online;
import io.chubao.joyqueue.toolkit.exception.Abnormity;
import io.chubao.joyqueue.toolkit.time.SystemClock;

import java.util.concurrent.Callable;

/**
 * 重试
 */
public abstract class Retry {

    /**
     * 循环重试，执行器可以实现Interrupt和Abnormit接口
     *
     * @param policy   重试策略
     * @param executor 回调函数
     * @param <T>
     * @return 返回结果
     * @throws Exception
     */
    public static <T> T execute(final RetryPolicy policy, final Callable<T> executor) throws Exception {
        if (executor == null) {
            throw new IllegalArgumentException("executor can not be null");
        }
        if (policy == null) {
            throw new IllegalArgumentException("policy can not be null");
        }
        int retryCount = 0;
        long startTime = SystemClock.now();
        boolean loop = true;
        Exception error = null;
        // 一致循环
        while (loop) {
            try {
                // 执行方法
                return executor.call();
            } catch (Exception e) {
                error = e;
                // 出现异常默认重试
                if (executor instanceof Abnormity) {
                    loop = ((Abnormity) executor).onException(e);
                }
                if (loop && executor instanceof Online) {
                    // 看看是否还要继续
                    loop = ((Online) executor).isStarted();
                }
                if (loop) {
                    // 计算下次重试时间点
                    long now = SystemClock.now();
                    long time = policy.getTime(now, ++retryCount, startTime);
                    if (time <= 0) {
                        // 不在重试
                        throw e;
                    }
                    // 休息一段事件
                    Thread.sleep(time - now);
                    if (executor instanceof Online) {
                        // 再次看看是否还要继续
                        loop = ((Online) executor).isStarted();
                    }
                }
            }
        }
        throw error;
    }

}