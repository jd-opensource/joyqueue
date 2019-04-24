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
package com.jd.journalq.toolkit.concurrent;

import com.jd.journalq.toolkit.time.SystemClock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by hexiaofeng on 14-6-24.
 */
public class RingBufferTest {

    private static final int total = 200000000;
    private static RingBuffer<SendRequest> buffer = new RingBuffer(10000, new RTEventHandler(), "Dispatch");
    private static AtomicLong sequences = new AtomicLong(0);
    private static AtomicLong producers = new AtomicLong(0);
    private static AtomicLong errors = new AtomicLong(0);
    private static AtomicLong consumers = new AtomicLong(0);
    private static CountDownLatch latch = new CountDownLatch(1);

    @BeforeClass
    public static void setup() throws Exception {
        buffer.start();
    }

    @AfterClass
    public static void tearDown() {
        buffer.stop();
    }

    @Test
    public void testAdd() throws InterruptedException {
        int threads = 10;
        int count = total / threads;
        int rest = total % threads;
        long startTime = SystemClock.now();
        for (int i = 0; i < threads; i++) {
            SendTask sendTask = new SendTask(i == (threads - 1) ? (count + rest) : count, startTime);
            Thread thread = new Thread(sendTask, "producer-" + i);
            thread.start();
        }
        latch.await();
        long endTime = SystemClock.now();
        System.out.println(String.format("producer=%d,consumer=%d,errors=%d,throughput=%d/s，%d/s", producers.get(),
                consumers.get(), errors.get(), producers.get() * 1000 / (endTime - startTime),
                consumers.get() * 1000 / (endTime - startTime)));
        System.out.println("add finished.");
    }

    protected static class RTEventHandler implements RingBuffer.EventHandler {
        @Override
        public void onEvent(Object[] elements) throws Exception {

            long count = consumers.addAndGet(elements.length);
            if (count + errors.get() == total) {
                latch.countDown();
            }
        }

        @Override
        public void onException(Throwable exception) {
            System.out.println(exception.getMessage());
        }

        @Override
        public int getBatchSize() {
            return 200;
        }

        @Override
        public long getInterval() {
            return 0;
        }
    }

    protected class SendTask implements Runnable {
        int count;
        long startTime;

        public SendTask(int count, long startTime) {
            this.count = count;
            this.startTime = startTime;
        }

        @Override
        public void run() {
            long endTime;
            long index;
            for (int i = 0; i < count; i++) {
                index = sequences.incrementAndGet();
                if (!buffer.add(new SendRequest(index), 50)) {
                    errors.incrementAndGet();
                } else {
                    producers.incrementAndGet();
                }
                if (index % 100000 == 0) {
                    endTime = SystemClock.now();
                    System.out.println(String.format("producer=%d,consumer=%d,errors=%d,throughput=%d/s(w)，%d/s(r)",
                            producers.get(), consumers.get(), errors.get(),
                            producers.get() * 1000 / (endTime - startTime),
                            consumers.get() * 1000 / (endTime - startTime)));
                }
            }
        }
    }

    protected class SendRequest {
        private long index;

        public SendRequest(long index) {
            this.index = index;
        }

        public long getIndex() {
            return index;
        }
    }
}
