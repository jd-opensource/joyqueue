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

import org.joyqueue.toolkit.time.SystemClock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by hexiaofeng on 16-6-29.
 */
public class SystemClockTest {

    ExecutorService executor = Executors.newFixedThreadPool(100);

    @Test
    public void testTime()throws Exception {
        List<Future> futureList = new ArrayList();
        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            futureList.add(executor.submit(() -> {
                try{
                    long time = SystemClock.now();
                    for (long j = 0; j < 100l; j++) {
                        SystemClock.now();
                    }
                    return SystemClock.now() - time;
                }finally {
                    latch.countDown();

                }
            }));
        }
        latch.await();

        long max =0;
        long total = 0;
        for (Future<Long> future:futureList){
            if (future.isDone()) {
                long cost = future.get();
                if (max < cost) {
                    max = cost;
                }
                total += cost;
            }
        }

        System.out.println("max:" + max );
        System.out.println("total:" + total);
        System.out.println("avg:" + total/100.0);

    }


    /**

     max:1060
     total:82143
     avg:821.43


     max:51439
     total:5086000
     avg:50860.0

     */



}
