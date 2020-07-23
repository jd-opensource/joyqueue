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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TimingWheel {

    private long tickMs;
    private int wheelSize;
    private long startMs;
    private AtomicInteger taskCounter;
    private DelayQueue<TimerTaskList> queue;
    private List<TimerTaskList> buckets = new ArrayList<TimerTaskList>();

    private long interval;
    private long currentTime;

    private volatile TimingWheel overflowWheel = null;

    public TimingWheel(long tickMs, int wheelSize, long startMs, AtomicInteger taskCounter, DelayQueue<TimerTaskList> queue) {
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.startMs = startMs;
        this.taskCounter = taskCounter;
        this.queue = queue;
        this.interval = tickMs * wheelSize;
        this.currentTime = startMs - (startMs % tickMs);
        for (int i = 0; i < wheelSize; i++) {
            buckets.add(new TimerTaskList(taskCounter));
        }
    }

    private void addOverflowWheel() {
        synchronized (this) {
            if (overflowWheel == null) {
                overflowWheel = new TimingWheel(interval, wheelSize, currentTime, taskCounter, queue);
            }
        }
    }

    protected boolean add(TimerTaskList.TimerTaskEntry timerTaskEntry) {
        long expiration = timerTaskEntry.timerTask.delayMs;

        if (timerTaskEntry.cancelled()) {
            return false;
        } else if (expiration < currentTime + tickMs) {
            return false;
        } else if (expiration < currentTime + interval) {
            long virtualId = expiration / tickMs;

            TimerTaskList bucket = buckets.get((int) (virtualId % wheelSize));
            bucket.add(timerTaskEntry);

            // Set the bucket expiration time
            if (bucket.setExpiration(virtualId * tickMs)) {
                // The bucket needs to be enqueued because it was an expired bucket
                // We only need to enqueue the bucket when its expiration time has changed, i.e. the wheel has advanced
                // and the previous buckets gets reused; further calls to set the expiration within the same wheel cycle
                // will pass in the same value and hence return false, thus the bucket with the same expiration will not
                // be enqueued multiple times.
                queue.offer(bucket);
            }
            return true;
        } else {
            // Out of the interval. Put it into the parent timer
            if (overflowWheel == null) {
                addOverflowWheel();
            }
            return overflowWheel.add(timerTaskEntry);
        }
    }

    protected void advanceClock(long timeMs) {
        if (timeMs >= currentTime + tickMs) {
            currentTime = timeMs - (timeMs % tickMs);

            // Try to advance the clock of the overflow wheel if present
            if (overflowWheel != null) overflowWheel.advanceClock(currentTime);
        }
    }
}

