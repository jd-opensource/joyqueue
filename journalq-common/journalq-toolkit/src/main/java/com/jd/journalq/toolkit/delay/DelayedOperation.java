/**
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
package com.jd.journalq.toolkit.delay;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class DelayedOperation extends TimerTask {

    private AtomicBoolean completed = new AtomicBoolean(false);

    public DelayedOperation(long delayMs) {
        // 不加当前时间导致 TimerTask 很快就触发
        this.delayMs = delayMs + System.currentTimeMillis();
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
     * Thread-safe variant of tryComplete(). This can be overridden if the operation provides its
     * own synchronization.
     */
    protected boolean safeTryComplete() {
        synchronized (this) {
            return tryComplete();
        }
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