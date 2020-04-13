/**
 * Partially copied from Apache Kafka.
 *
 * Original LICENSE :
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.toolkit.delay;

import org.joyqueue.toolkit.time.SystemClock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class DelayedOperation extends TimerTask {

    private AtomicBoolean completed = new AtomicBoolean(false);
    private AtomicBoolean tryCompletePending = new AtomicBoolean(false);
    private Lock lock;

    public DelayedOperation(long delayMs) {
        this(delayMs, new ReentrantLock());
    }

    public DelayedOperation(long delayMs, Lock lock) {
        // 不加当前时间导致 TimerTask 很快就触发
        this.delayMs = delayMs + SystemClock.now();
        this.lock = lock;
    }

    /*
     * Force completing the delayed operation, if not already completed.
     * This function can be triggered when
     *
     * 1. The operation has been verified to be completable inside tryComplete()
     * 2. The operation has expired and hence needs to be completed right now
     *
     * Return true if the operation is completed by the caller: note that
     * concurrent threads can try to complete the same operation, but only
     * the first thread will succeed in completing the operation and return
     * true, others will still return false
     */
    public boolean forceComplete() {
        if (completed.compareAndSet(false, true)) {
            // cancel the timeout timer
            cancel();
            onComplete();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if the delayed operation is already completed
     */
    protected boolean isCompleted() {
        return completed.get();
    }

    /**
     * Call-back to execute when a delayed operation gets expired and hence forced to complete.
     */
    protected abstract void onExpiration();

    /**
     * Process for completing an operation; This function needs to be defined
     * in subclasses and will be called exactly once in forceComplete()
     */
    protected abstract void onComplete();

    /**
     * Try to complete the delayed operation by first checking if the operation
     * can be completed by now. If yes execute the completion logic by calling
     * forceComplete() and return true iff forceComplete returns true; otherwise return false
     * <p>
     * This function needs to be defined in subclasses
     */
    protected abstract boolean tryComplete();

    /**
     * Thread-safe variant of tryComplete() that attempts completion only if the lock can be acquired
     * without blocking.
     *
     * If threadA acquires the lock and performs the check for completion before completion criteria is met
     * and threadB satisfies the completion criteria, but fails to acquire the lock because threadA has not
     * yet released the lock, we need to ensure that completion is attempted again without blocking threadA
     * or threadB. `tryCompletePending` is set by threadB when it fails to acquire the lock and at least one
     * of threadA or threadB will attempt completion of the operation if this flag is set. This ensures that
     * every invocation of `maybeTryComplete` is followed by at least one invocation of `tryComplete` until
     * the operation is actually completed.
     */
    protected boolean maybeTryComplete() {
        boolean retry = false;
        boolean done = false;
        do {
            if (lock.tryLock()) {
                try {
                    tryCompletePending.set(false);
                    done = tryComplete();
                } finally {
                    lock.unlock();
                }
                // While we were holding the lock, another thread may have invoked `maybeTryComplete` and set
                // `tryCompletePending`. In this case we should retry.
                retry = tryCompletePending.get();
            } else {
                // Another thread is holding the lock. If `tryCompletePending` is already set and this thread failed to
                // acquire the lock, then the thread that is holding the lock is guaranteed to see the flag and retry.
                // Otherwise, we should set the flag and retry on this thread since the thread holding the lock may have
                // released the lock and returned by the time the flag is set.
                retry = !tryCompletePending.getAndSet(true);
            }
        } while (!isCompleted() && retry);
        return done;
    }

    /*
     * run() method defines a task that is executed on timeout
     */
    @Override
    public void run() {
        if (forceComplete()) {
            onExpiration();
        }
    }

}