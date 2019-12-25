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

import org.joyqueue.toolkit.time.SystemClock;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 锁工具包
 */
public class Locks {
    /**
     * 锁失败
     */
    public static final int LOCK_FAIL = -2;

    /**
     * 锁
     *
     * @param lock    锁
     * @param timeout 超时时间
     *                <ul>
     *                <li> &gt;0 等待超时时间</li>
     *                <li> =0 无限等待</li>
     *                <li> &lt;0 无限等待</li>
     *                </ul>
     * @return 剩余的超时时间
     * <ul>
     * <li> &gt;0 锁成功，timeout&gt;0，剩余超时时间</li>
     * <li>0 锁成功，timeout=0</li>
     * <li>-1 锁成功，timeout&lt;0</li>
     * <li>-2 失败</li>
     * </ul>
     */
    public static long tryLock(final Lock lock, final long timeout) {
        long time;
        if (timeout > 0) {
            time = SystemClock.now();
            try {
                if (lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                    // 锁成功，获取剩余时间
                    time = timeout - (SystemClock.now() - time);
                    if (time > 0) {
                        // 还有剩余时间
                        return time;
                    } else {
                        // 没用剩余时间，则释放锁
                        lock.unlock();
                    }
                }
                // 没用锁成功
                return LOCK_FAIL;
            } catch (InterruptedException e) {
                // 当前线程终止
                Thread.currentThread().interrupt();
                return LOCK_FAIL;
            }
        } else {
            lock.lock();
            return timeout == 0 ? 0 : -1;
        }
    }

    /**
     * 等待
     *
     * @param lock        锁
     * @param timeout     超时时间
     *                    <ul>
     *                    <li> &gt;0 等待超时时间</li>
     *                    <li> =0 无限等待</li>
     *                    </ul>
     * @param doubleCheck 拿到锁后进行二次校验，如果满足条件则不需要等待
     * @return 是否成功
     */
    public static boolean awaitQuiet(final Lock lock, final Condition condition, final long timeout,
            final Callable<Boolean> doubleCheck) {
        boolean locked = false;
        try {
            if (timeout > 0) {
                long time = SystemClock.now();
                if (lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                    locked = true;
                    // 锁成功，获取剩余时间
                    time = timeout - (SystemClock.now() - time);
                    if (time > 0) {
                        // 还有剩余时间
                        if (doubleCheck == null || !doubleCheck.call()) {
                            condition.await(time, TimeUnit.MILLISECONDS);
                        }
                        return true;
                    } else {
                        // 没用剩余时间，则释放锁
                        return false;
                    }
                }
                // 没用锁成功
                return false;
            } else {
                lock.lock();
                locked = true;
                condition.await();
                return true;
            }
        } catch (InterruptedException e) {
            // 当前线程终止
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    /**
     * 等待时间
     *
     * @param condition 信号量
     * @param time      时间
     * @return 是否成功
     */
    public static boolean awaitQuiet(final Condition condition, final long time) {
        if (condition == null) {
            return false;
        }
        try {
            condition.await(time, TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 等待时间
     *
     * @param condition 信号量
     * @return 是否成功
     */
    public static boolean awaitQuiet(final Condition condition) {
        if (condition == null) {
            return false;
        }
        try {
            condition.await();
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 等待信号量，不抛出异常
     *
     * @param mutex 信号量
     * @throws InterruptedException
     */
    public static void await(final Object mutex) throws InterruptedException {
        if (mutex == null) {
            return;
        }
        // 等待一段时间
        synchronized (mutex) {
            mutex.wait();
        }
    }

    /**
     * 等待信号量，不抛出异常
     *
     * @param mutex 信号量
     * @return 是否成功
     */
    public static boolean awaitQuiet(final Object mutex) {
        if (mutex == null) {
            return false;
        }
        // 等待一段时间
        synchronized (mutex) {
            try {
                mutex.wait();
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    /**
     * 等待信号量，不抛出异常
     *
     * @param mutex 信号量
     * @param time  时间
     * @throws InterruptedException
     */
    public static void await(final Object mutex, final long time) throws InterruptedException {
        if (mutex == null) {
            return;
        }
        // 等待一段时间
        synchronized (mutex) {
            mutex.wait(time);
        }
    }

    /**
     * 等待信号量，不抛出异常
     *
     * @param mutex 信号量
     * @param time  时间
     * @return 是否成功
     */
    public static boolean awaitQuiet(final Object mutex, final long time) {
        if (mutex == null) {
            return false;
        }
        // 等待一段时间
        synchronized (mutex) {
            try {
                mutex.wait(time);
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    /**
     * 信号通知
     *
     * @param lock      锁
     * @param condition 信号量
     */
    public static void signalAll(final Lock lock, final Condition condition) {
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 信号通知
     *
     * @param lock       锁
     * @param conditions 信号量
     */
    public static void signalAll(final Lock lock, final Condition... conditions) {
        lock.lock();
        try {
            for (Condition condition : conditions) {
                condition.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 通知
     *
     * @param mutex 信号量
     *
     */

    public static void notify(final Object mutex) {
        if (mutex == null) {
            return;
        }
        synchronized (mutex) {
            mutex.notify();
        }
    }

    /**
     * 通知
     *
     * @param mutex 信号量
     */
    public static void notifyAll(final Object mutex) {
        if (mutex == null) {
            return;
        }
        synchronized (mutex) {
            mutex.notifyAll();
        }
    }
}
