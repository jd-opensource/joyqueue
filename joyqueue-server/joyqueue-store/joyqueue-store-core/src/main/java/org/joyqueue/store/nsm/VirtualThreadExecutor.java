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
package org.joyqueue.store.nsm;

import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 设计这个虚拟线程执行器的设计动机是用少量线程模拟大量线程。
 * <p>
 * 主要适用于如下场景：
 * 大量线程各自处理不同的任务，这些线程的工作是循环等待任务到来，任务到来后立即执行任务，周而复始。
 * 这些线程大部分时间都在等待任务到来，少部分时间在执行任务。
 * <p>
 * 设计思想：
 * 既然线程的大部分时间都在等待，只有少部分时间是执行任务，就可以省略等待时间，用少量的线程依次这些任务。
 * <p>
 * 为什么不用内置的Executor？
 * 使用Executor会带来并发问题，例如：原来同一个线程依次执行任务A、B，使用Executor后，有可能任务A、B
 * 被分配到了不同的线程，变成了并发执行，引起并发问题。
 * <p>
 * 用法：
 * 和普通的线程类似，每个虚拟线程VirtualThread，有一个run()方法，用于执行任务。
 * 每执行一次run()方法处理一个任务，如果有任务并且处理了任务返回true，否则返回false；
 * 实现的业务逻辑类似于：
 * boolean run() {
 * if(hasTaskTodo()) {
 * doMyTask();
 * return true;
 * }
 * return false;
 * }
 * 实现中不要有wait和block，占用过长的线程时间。
 * <p>
 * <p>
 * <p>
 * 实现思路：
 * 所有虚拟线程都被封装成一个DelayCommand对象放入延时队列commandQueue。
 * 所有物理线程从延时队列中出队一个DelayCommand，执行一次DelayCommand.virtualThread.run()方法
 * 如果run()方法返回true，说明刚刚处理了一个任务，VirtualThreadExecutor会无等待的继续反复执行run()方法继续处理下一个任务，
 * 直到run()方法返回false暂时没有任务可做了，或者达到maxUseTime每次最大占用物理线程的时长。
 * <p>
 * 计算等待时长后，放入延时队列。
 * <p>
 * 计算等待时长的思路：
 * 如果虚拟线程刚刚执行了一个任务，那接下来的一小段时间内还有任务的可能性较大，不等待。
 * 如果虚拟线程连续多次没有任务执行，接下来有任务来的可能性较小，那就逐渐将每次的等待增大，将线程时间让给其它虚拟线程。
 * <p>
 * 计算等待时长的算法：
 * 如果上次执行任务返回true或者在空转期，等待时长归零，尽快执行下一次。
 * 如果连续多次返回false，并且已经过了空转期，每次增加一点儿等待时长，直到达到最大等待时长。
 *
 * @author liyue25
 * Date: 2018-12-19
 */
public class VirtualThreadExecutor {
    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadExecutor.class);

    private final long keepAliveTimeMs, maxIntervalMs;
    private final int steps;
    private final long maxUseTime;
    private final DelayQueue<DelayCommand> commandQueue = new DelayQueue<>();
    private final List<Thread> workThreads;
    private final Set<VirtualThread> toBeRemoved = ConcurrentHashMap.newKeySet();
    private final Set<VirtualThread> virtualThreads = ConcurrentHashMap.newKeySet();

    /**
     * @param keepAliveTimeMs 每次执行完任务后线程空转的时长，在空转期内一旦有任务立即就能执行，避免等待；
     * @param maxIntervalMs   最大等待时长
     * @param steps           递增次数
     * @param maxUseTime      虚拟线程每次最大占用物理线程的时长
     * @param threadCount     物理线程数量
     */
    public VirtualThreadExecutor(long keepAliveTimeMs, long maxIntervalMs, int steps, long maxUseTime, int threadCount) {
        this.keepAliveTimeMs = keepAliveTimeMs;
        this.maxIntervalMs = maxIntervalMs;
        this.steps = steps;
        this.maxUseTime = maxUseTime;
        workThreads = IntStream.range(0, threadCount)
                .mapToObj(index -> {
                    Thread thread = new Thread(new WorkThread());
                    thread.setName("VirtualThreadExecutor-" + index);
                    thread.start();
                    return thread;
                }).collect(Collectors.toList());
    }

    public void start(VirtualThread vt, String name) {
        virtualThreads.add(vt);
        commandQueue.add(new DelayCommand(vt, name));
    }

    public void start(VirtualThread vt, long minDelayMs, String name) {
        virtualThreads.add(vt);
        commandQueue.add(new DelayCommand(vt, minDelayMs, name));
    }

    public void stop(VirtualThread vt) throws InterruptedException {
        if (virtualThreads.remove(vt)) {
            toBeRemoved.add(vt);
            if (commandQueue.removeIf(cmd -> cmd.virtualThread == vt)) {
                toBeRemoved.remove(vt);
            } else {
                while (toBeRemoved.contains(vt)) {
                    Thread.sleep(50L);
                }
            }
        }
    }

    private void wait(Thread thread) {
        logger.info("Stopping thread {}...", thread.getName());
        long t0 = SystemClock.now();
        long timeout = 1000L;
        while (SystemClock.now() - t0 < timeout && thread.isAlive()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void stop() {
        workThreads.forEach(Thread::interrupt);
        workThreads.forEach(this::wait);
    }

    private static class DelayCommand implements Delayed {
        private final VirtualThread virtualThread;
        private final long minDelayMs;
        private final String name;
        private volatile long startTime = SystemClock.now();
        private volatile long lastRunTime = SystemClock.now(); // 上一次有效运行的结束时间
        private volatile long delay = 0;

        private DelayCommand(VirtualThread virtualThread, String name) {
            this(virtualThread, 0L, name);

        }

        private DelayCommand(VirtualThread virtualThread, long minDelayMs, String name) {
            this.virtualThread = virtualThread;
            this.minDelayMs = minDelayMs;
            this.name = name;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long diff = startTime - SystemClock.now();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed another) {
            if (this.startTime < ((DelayCommand) another).startTime) {
                return -1;
            }
            if (this.startTime > ((DelayCommand) another).startTime) {
                return 1;
            }
            return 0;

        }

    }

    private class WorkThread implements Runnable {
        @Override
        public void run() {
            DelayCommand cmd = null;
            boolean dryRun = true;
            while (!Thread.currentThread().isInterrupted()) {

                try {
                    cmd = commandQueue.take();
                    if (toBeRemoved.remove(cmd.virtualThread)) {
                        continue;
                    }
                    long start = SystemClock.now();
                    dryRun = true;
                    while (maxUseTime + start > SystemClock.now()) {
                        if (cmd.virtualThread.run()) {
                            if (dryRun) dryRun = false;
                        } else {
                            break;
                        }
                        Thread.yield();
                    }
                } catch (InterruptedException e) {
                    logger.warn("Virtual thread interrupted!");
                    break;
                } catch (Throwable e) {
                    logger.warn("Exception on {} :", cmd == null ? "" : cmd.name, e);
                }
                if (null != cmd) {
                    long now = SystemClock.now();
                    if (dryRun) {
                        if (keepAliveTimeMs + cmd.lastRunTime <= now) {
                            if (cmd.delay < maxIntervalMs) {
                                cmd.delay += maxIntervalMs / steps;
                            }
                        }
                    } else {
                        cmd.delay = cmd.minDelayMs;
                        cmd.lastRunTime = now;
                    }
                    cmd.startTime = now + cmd.delay;
                    commandQueue.put(cmd);
                }
            }
        }

    }

}
