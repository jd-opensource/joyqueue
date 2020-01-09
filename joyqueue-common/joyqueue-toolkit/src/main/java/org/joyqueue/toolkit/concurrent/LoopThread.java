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

import org.joyqueue.toolkit.lang.LifeCycle;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 一个后台线程，实现类似：
 * while(true){
 *     doWork();
 * }
 * 的线程。
 */
public abstract class LoopThread implements Runnable,LifeCycle{
    private Thread thread = null;
    private String name;
    protected long minSleep = 50L,maxSleep = 500L;
    private boolean daemon;
    private final Lock wakeupLock = new ReentrantLock();
    private final java.util.concurrent.locks.Condition wakeupCondition = wakeupLock.newCondition();
    private static final int STATE_STOPPED = 0;
    private static final int STATE_STOPPING = 1;
    private static final int STATE_STARTING = 2;
    private static final int STATE_RUNNING = 3;
    private AtomicInteger state = new AtomicInteger(STATE_STOPPED);
    private AtomicBoolean needToWakeUp = new AtomicBoolean(false);
    /**
     * 每次循环需要执行的代码。
     */
    abstract void doWork() throws Throwable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    /**
     * doWork() 前判断是否满足条件。
     * @return true: 执行doWork。
     */
    protected boolean condition() {
        return true;
    }

    @Override
    public synchronized void start() {
        if(!isStarted()) {
            state.set(STATE_STARTING);
            thread = new Thread(this);
            thread.setName(name == null ? "LoopThread": name);
            thread.setDaemon(daemon);
            thread.start();
        }
    }

    @Override
    public synchronized void stop() {

        if(state.get() != STATE_STOPPED) {
            state.set(STATE_STOPPING);
            thread.interrupt();
            while (state.get() != STATE_STOPPED) {
                try {
                    wakeup();
                    Thread.sleep(10L);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    @Override
    public boolean isStarted() {
        return state.get() == STATE_RUNNING;
    }

    @Override
    public void run() {
        if(state.compareAndSet(STATE_STARTING, STATE_RUNNING) ) {
            while (state.get() == STATE_RUNNING) {

                long t0 = System.nanoTime();
                try {
                    if(condition()) {
                        doWork();
                    }

                } catch (InterruptedException i) {
                    Thread.currentThread().interrupt();
                } catch (Throwable t) {
                    if (!handleException(t)) {
                        break;
                    }
                }
                try {
                    long t1 = System.nanoTime();

                    // 为了避免空转CPU高，如果执行时间过短，等一会儿再进行下一次循环
                    if (t1 - t0 < minSleep * 1000000L) {

                        wakeupLock.lock();
                        try {
                            needToWakeUp.set(true);
                            wakeupCondition.await(minSleep < maxSleep ? ThreadLocalRandom.current().nextLong(minSleep, maxSleep) : minSleep, TimeUnit.MILLISECONDS);

                        } finally {
                            wakeupLock.unlock();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        state.set(STATE_STOPPED);
    }

    /**
     * 唤醒任务如果任务在Sleep
     */
    public synchronized void wakeup() {
        if(needToWakeUp.compareAndSet(true, false)) {
            wakeupLock.lock();
            try {
                wakeupCondition.signal();
            } finally {
                wakeupLock.unlock();
            }
        }
    }

    /**
     * 处理doWork()捕获的异常
     * @return true：继续循环，false：结束线程
     */
    protected boolean handleException(Throwable t) {
        return true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public interface  Worker {
        void doWork() throws Throwable;
    }

    public interface ExceptionHandler {
        boolean handleException(Throwable t);
    }
    public interface ExceptionListener {
        void onException(Throwable t);
    }
    public interface Condition{
        boolean condition();
    }

    public static class Builder {
        private String name;
        private long minSleep = -1L,maxSleep = -1L;
        private Boolean daemon;
        private Worker worker;
        private ExceptionHandler exceptionHandler;
        private ExceptionListener exceptionListener;
        private Condition condition;

        public Builder doWork(Worker worker){
            this.worker = worker;
            return this;
        }

        public Builder handleException(ExceptionHandler exceptionHandler){
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        public Builder onException(ExceptionListener exceptionListener){
            this.exceptionListener = exceptionListener;
            return this;
        }



        public Builder name(String name){
            this.name = name;
            return this;
        }

        public Builder sleepTime(long minSleep, long maxSleep){
            this.minSleep = minSleep;
            this.maxSleep = maxSleep;
            return this;
        }

        public Builder daemon(boolean daemon) {
            this.daemon = daemon;
            return this;
        }

        public Builder condition(Condition condition){
            this.condition = condition;
            return this;
        }

        public LoopThread build(){
            LoopThread loopThread = new LoopThread() {
                @Override
                void doWork() throws Throwable{
                    worker.doWork();
                }

                @Override
                protected boolean handleException(Throwable t) {
                    if(null != exceptionListener) exceptionListener.onException(t);
                    if(null != exceptionHandler) {
                        return exceptionHandler.handleException(t);
                    }else {
                        return super.handleException(t);
                    }
                }

                @Override
                protected boolean condition() {
                    if(null != condition) {
                        return condition.condition();
                    }else {
                        return super.condition();
                    }
                }
            };
            if(null != name) loopThread.setName(name);
            if(null != daemon) loopThread.setDaemon(daemon);
            if(this.minSleep >= 0) loopThread.minSleep = this.minSleep;
            if(this.maxSleep >= 0) loopThread.maxSleep = this.maxSleep;
            return loopThread;
        }


    }
}