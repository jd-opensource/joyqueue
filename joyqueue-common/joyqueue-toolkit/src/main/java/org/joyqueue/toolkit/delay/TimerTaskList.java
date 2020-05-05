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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TimerTaskList forms a doubly linked cyclic list using a dummy root entry
 * root.next points to the head
 * root.prev points to the tail
 */
//@ThreadSafe
public class TimerTaskList implements Delayed {

    private final TimerTaskEntry root;

    private final AtomicLong expiration = new AtomicLong(-1L);

    private final AtomicInteger taskCounter;

    public TimerTaskList(AtomicInteger taskCounter) {
        this.root = new TimerTaskEntry(null);
        this.root.next = root;
        this.root.prev = root;
        this.taskCounter = taskCounter;
    }

    // Set the bucket's expiration time
    // Returns true if the expiration time is changed
    public boolean setExpiration(long expirationMs) {
        return expiration.getAndSet(expirationMs) != expirationMs;
    }

    // Get the bucket's expiration time
    public long getExpiration() {
        return expiration.get();
    }

    // Add a timer task entry to this list
    public void add(TimerTaskEntry timerTaskEntry) {
        boolean done = false;
        while (!done) {
            // Remove the timer task entry if it is already in any other list
            // We may retry until timerTaskEntry.list becomes null.
            timerTaskEntry.remove();

            synchronized (this) {
                if (timerTaskEntry.list == null) {
                    // put the timer task entry to the end of the list. (root.prev points to the tail entry)
                    TimerTaskEntry tail = root.prev;
                    timerTaskEntry.next = root;
                    timerTaskEntry.prev = tail;
                    timerTaskEntry.list = this;
                    tail.next = timerTaskEntry;
                    root.prev = timerTaskEntry;
                    taskCounter.incrementAndGet();
                    done = true;
                }
            }
        }
    }

    // Remove the specified timer task entry from this list
    public void remove(TimerTaskEntry timerTaskEntry) {
        synchronized (this) {
            if (timerTaskEntry.list == this) {
                timerTaskEntry.next.prev = timerTaskEntry.prev;
                timerTaskEntry.prev.next = timerTaskEntry.next;
                timerTaskEntry.next = null;
                timerTaskEntry.prev = null;
                timerTaskEntry.list = null;
                taskCounter.decrementAndGet();
            }
        }
    }

    // Remove all task entries and apply the supplied function to each of them
    public void flush(Timer timer) {
        synchronized (this) {
            TimerTaskEntry head = root.next;
            while (head != root) {
                remove(head);
                timer.addTimerTaskEntry(head);
                head = root.next;
            }
            expiration.set(-1L);
        }
    }

    public long getDelay(TimeUnit unit) {
        return unit.convert(Math.max(getExpiration() - SystemClock.now(), 0), TimeUnit.MILLISECONDS);
    }

    public int compareTo(Delayed d) {

        TimerTaskList other = (TimerTaskList) d;

        if (getExpiration() < other.getExpiration()) {
            return -1;
        } else if (getExpiration() > other.getExpiration()) {
            return 1;
        } else {
            return 0;
        }
    }

    public static class TimerTaskEntry {

        private volatile TimerTaskList list = null;
        private TimerTaskEntry next = null;
        private TimerTaskEntry prev = null;

        public TimerTask timerTask;

        public TimerTaskEntry(TimerTask timerTask) {
            this.timerTask = timerTask;
            // if this timerTask is already held by an existing timer task entry,
            // setTimerTaskEntry will remove it.
            if (this.timerTask != null) {
                this.timerTask.setTimerTaskEntry(this);
            }
        }

        public boolean cancelled() {
            return timerTask.getTimerTaskEntry() != this;
        }

        public void remove() {
            TimerTaskList currentList = list;
            // If remove is called when another thread is moving the entry from a task entry list to another,
            // this may fail to remove the entry due to the change of value of list. Thus, we retry until the list becomes null.
            // In a rare case, this thread sees null and exits the loop, but the other thread insert the entry to another list later.
            while (currentList != null) {
                currentList.remove(this);
                currentList = list;
            }
        }

    }
}
