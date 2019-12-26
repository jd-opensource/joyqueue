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
package org.joyqueue.toolkit.time;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
//CHECKSTYLE.OFF: RegexpMultiline
/**
 * {@link SystemClock} is a optimized substitute of {@link System#currentTimeMillis()} for avoiding config switch
 * overload.
 * Every instance would start a concurrent to update the time, so it's supposed to be singleton in application config.
 */
public class SystemClock {

    private static final SystemClock instance = new SystemClock();
    // 精度(毫秒)
    private final long precision;
    // 当前时间
    private final AtomicLong now;
    // 调度任务
    private ScheduledExecutorService scheduler;

    public static SystemClock getInstance() {
        return instance;
    }

    public SystemClock() {
        this(1L);
    }
    //CHECKSTYLE:OFF
    public SystemClock(long precision) {
        this.precision = precision;
        now = new AtomicLong(System.currentTimeMillis());
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "SystemClock");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(new Timer(now), precision, precision, TimeUnit.MILLISECONDS);
    }
    //CHECKSTYLE:ON

    public long getTime() {
        return now.get();
    }

    /**
     * 获取当前时钟
     *
     * @return 当前时钟
     */
    public static long now() {
        return instance.getTime();
    }

    public long precision() {
        return precision;
    }

    protected static class Timer implements Runnable {
        // 注入进来，避免访问SystemClock.now占用很多CPU
        private final AtomicLong now;

        private Timer(AtomicLong now) {
            this.now = now;
        }

        @Override
        public void run() {
            now.set(System.currentTimeMillis());
        }
    }
}
//CHECKSTYLE.ON: RegexpMultiline