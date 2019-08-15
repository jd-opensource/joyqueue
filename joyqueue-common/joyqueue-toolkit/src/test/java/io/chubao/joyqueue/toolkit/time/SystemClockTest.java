package io.chubao.joyqueue.toolkit.time;

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
                    for (long j = 0; j < 100000000l; j++) {
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
