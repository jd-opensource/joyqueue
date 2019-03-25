package com.jd.journalq.toolkit.promise;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author liyue25
 * Date: 2018/10/29
 */
public class PromiseTest {

    @Test
    public void simplePromiseTest() throws InterruptedException {
        class Wrap {
          int value;
        }
        final Wrap wrap = new Wrap();
        wrap.value = 6;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(1);


        Promise
                .promise(executorService,() -> wrap)
                .then(w -> w.value = 10 * w.value)
                .then(() -> {
                    wrap.value = 10 * wrap.value;
                    return wrap;
                })
                .then(w ->latch.countDown())
                .submit();
        latch.await();
        Assert.assertEquals(600,wrap.value);
    }

    @Test
    public void simpleExceptionHandleTest() throws InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(1);

        class Wrap {
            Exception e;
            int value;
        }
        final Wrap wrap = new Wrap();
        wrap.value = 1;
        Promise
                .promise(executorService,()-> {
                    throw new Exception("E1");
                })
                .then(o -> {   // can not reach here
                    wrap.value = 2;
                    return wrap;
                })
                .handle((p, e)-> {
                    wrap.e = e;
                    latch.countDown();
                    return true;
                })
                .submit();
        latch.await();
        Assert.assertEquals("E1", wrap.e.getMessage());
        Assert.assertEquals(1, wrap.value);
    }

    @Test
    public void concurrentTest() throws IndexOutOfBoundsException, InterruptedException {

        int concurrentCount = 1000;
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(concurrentCount);
        List<Integer> list = new ArrayList<>(concurrentCount);
        for (int i = 0; i <concurrentCount; i++) {
            list.add(i);


            int finalI = i;
            Promise.promise(executorService,() -> 10 * list.get(finalI))
                    .then(n -> 10 * n)
                    .then(n -> list.set(finalI, n))
                    .then(latch::countDown)
                    .submit();
        }

        latch.await();

        Assert.assertEquals(concurrentCount,list.size());

        for (int i = 0; i < concurrentCount; i++) {
            Assert.assertEquals(i * 100, (int) list.get(i));
        }


    }

    private int counter;
    private long t;
    @Test
    public void performanceTest() {
        ExecutorService  executorService = new ThreadPoolExecutor(4,4,1,TimeUnit.SECONDS,new ArrayBlockingQueue<>(10240),new ThreadPoolExecutor.CallerRunsPolicy());
        ExecutorService  executorService1 = new ThreadPoolExecutor(4,4,1,TimeUnit.SECONDS,new ArrayBlockingQueue<>(10240),new ThreadPoolExecutor.CallerRunsPolicy());
//        ExecutorService  executorService = Executors.newFixedThreadPool(8);
//        ExecutorService  executorService = Executors.newSingleThreadExecutor();
        counter = 0;
        t = System.currentTimeMillis();
        for (int i = 0; i <1024 * 1024 * 1024; i++) {
//            int c = counter++;
//            if(c % (1024 * 1024) == 0) {
//                long t1 = System.currentTimeMillis();
//                if(t1 > t) {
//                    System.out.println("QPS: " + (1024 * 1024 * 1000 / (t1 - t)));
//                    t = t1;
//                }
//            }


            Promise.promise(executorService,() -> counter++).then(executorService1,c->{
                if(c % (1024 * 1024) == 0) {
                    long t1 = System.currentTimeMillis();
                    if(t1 > t) {
                        System.out.println("QPS: " + (1024 * 1024 * 1000 / (t1 - t)));
                        t = t1;
                    }
                }
            }).submit();

//            executorService.submit(() -> {
//                int c = counter++;
//                if(c % (1024 * 1024) == 0) {
//                    long t1 = System.currentTimeMillis();
//                    if(t1 > t) {
//                        System.out.println("QPS: " + (1024 * 1024 * 1000 / (t1 - t)));
//                        t = t1;
//                    }
//                }
//            });
        }

    }
}
