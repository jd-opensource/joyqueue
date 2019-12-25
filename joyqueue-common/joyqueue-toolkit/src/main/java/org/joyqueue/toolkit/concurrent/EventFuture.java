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
package org.joyqueue.toolkit.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author liyue25
 * Date: 2018/11/9
 */
public class EventFuture<R> implements Future<R>,EventListener<R>{

    private R result = null;
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private volatile boolean done = false;


    /**
     * 事件处理，不要抛出异常
     *
     * @param event 事件
     */
    @Override
    public void onEvent(R event) {
        lock.lock();
        try {
            if(!done) {
                    result = event;
                    done = true;
                    condition.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Attempts to cancel execution of this task.  This attempt will
     * fail if the task has already completed, has already been cancelled,
     * or could not be cancelled for some other reason. If successful,
     * and this task has not started when {@code cancel} is called,
     * this task should never run.  If the task has already started,
     * then the {@code mayInterruptIfRunning} parameter determines
     * whether the thread executing this task should be interrupted in
     * an attempt to stop the task.
     *
     * <p>After this method returns, subsequent calls to {@link #isDone} will
     * always return {@code true}.  Subsequent calls to {@link #isCancelled}
     * will always return {@code true} if this method returned {@code true}.
     *
     * @param mayInterruptIfRunning {@code true} if the thread executing this
     *                              task should be interrupted; otherwise, in-progress tasks are allowed
     *                              to complete
     * @return {@code false} if the task could not be cancelled,
     * typically because it has already completed normally;
     * {@code true} otherwise
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        // not supported
        return false;
    }

    /**
     * Returns {@code true} if this task was cancelled before it completed
     * normally.
     *
     * @return {@code true} if this task was cancelled before it completed
     */
    @Override
    public boolean isCancelled() {
        return false;
    }

    /**
     * Returns {@code true} if this task completed.
     * <p>
     * Completion may be due to normal termination, an exception, or
     * cancellation -- in all of these cases, this method will return
     * {@code true}.
     *
     * @return {@code true} if this task completed
     */
    @Override
    public boolean isDone() {
        return done;
    }

    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.
     *
     * @return the computed result
     * @throws java.util.concurrent.CancellationException if the computation was cancelled
     * @throws InterruptedException  if the current thread was interrupted
     *                               while waiting
     */
    @Override
    public R get() throws InterruptedException {
        lock.lock();
        try {
            if(!done) {
                condition.await();
            }
        }finally {
            lock.unlock();
        }
        return result;
    }

    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result, if available.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return the computed result
     * @throws InterruptedException  if the current thread was interrupted
     *                               while waiting
     */
    @Override
    public R get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        lock.lock();
        try {
            if(!done) {
                if (!condition.await(timeout, unit)) {
                    throw new TimeoutException();
                }
            }
        }finally {
            lock.unlock();
        }

        return result;
    }
}
