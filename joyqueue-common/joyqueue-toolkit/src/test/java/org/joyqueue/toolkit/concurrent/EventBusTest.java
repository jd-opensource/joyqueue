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

import org.joyqueue.toolkit.concurrent.EventBus;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hexiaofeng on 16-5-17.
 */
public class EventBusTest {

    @Test
    public void testEventBus() throws Exception {
        final AtomicInteger counter = new AtomicInteger(0);
        EventBus<Integer> eventBus = new EventBus<Integer>(null, new EventListener<Integer>() {
            @Override
            public void onEvent(Integer event) {
                counter.set(event);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            }
        });
        eventBus.start();
        eventBus.add(1);
        eventBus.add(2);
        eventBus.add(3);
        eventBus.stop(true);
        Assert.assertEquals(counter.get(), 3);
    }

    @Test
    public void testHeartbeat() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        EventBus<Integer> eventBus = new EventBus<Integer>(null, new MyEventListener(latch));
        eventBus.start();
        latch.await();
        eventBus.stop(true);
    }

    protected class MyEventListener implements EventListener<Integer>, EventListener.Heartbeat {

        private CountDownLatch latch;
        private long last;

        public MyEventListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onEvent(Integer event) {
            if (event == null && latch.getCount() > 0) {
                latch.countDown();
            }
        }

        @Override
        public boolean trigger(final long now) {
            if (now - last > 5000) {
                last = now;
                return true;
            }
            return false;
        }
    }
}
