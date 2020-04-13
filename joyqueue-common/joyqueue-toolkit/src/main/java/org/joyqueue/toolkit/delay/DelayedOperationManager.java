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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class DelayedOperationManager<T extends DelayedOperation> {

    private Timer timeoutTimer;
    private String purgatoryName;
    private int purgeInterval = 1000;
    private int watchers = 512;

    private ExecutorService taskExecutor;

    private List<WatchersList> watchersList;
    // the number of estimated total operations in the purgatory
    private AtomicInteger estimatedTotalOperations = new AtomicInteger(0);

    /* background thread expiring operations that have timed out */
    private ExpiredOperationReaper expirationReaper;

    public DelayedOperationManager(final String purgatoryName) {
        this(purgatoryName, 1000, true);
    }

    public DelayedOperationManager(final String purgatoryName, int purgeInterval, boolean reaperEnable) {
        this.taskExecutor = Executors.newFixedThreadPool(1, new ThreadFactory() {
            public Thread newThread(Runnable r) {
                NamedThreadFactory threadFactory = new NamedThreadFactory("joyqueue-delayed-operation-executor-" + purgatoryName);
                Thread thread = threadFactory.newThread(r);
                return thread;
            }
        });
        this.purgatoryName = purgatoryName;
        this.timeoutTimer = new Timer(this.taskExecutor);
        this.purgeInterval = purgeInterval;
        this.watchersList = initWatchersList();
    }

    /**
     * start the expire reaper thread
     */
    public void start() {
        expirationReaper = new ExpiredOperationReaper(String.format("ExpirationReaper-%s", purgatoryName));
        expirationReaper.start();
    }

    /**
     * Shutdown the expire reaper thread
     */
    public void shutdown() {
        if (expirationReaper != null) {
            expirationReaper.shutdown();
        }
        if (taskExecutor != null) {
            taskExecutor.shutdown();
        }
    }

    protected List<WatchersList> initWatchersList() {
        List<WatchersList> watchersList = Lists.newArrayListWithCapacity(watchers);
        for (int i = 0; i < watchers; i++) {
            watchersList.add(new WatchersList());
        }
        return watchersList;
    }

    protected WatchersList selectWatchersList(Object key) {
        return watchersList.get(Math.abs(key.hashCode() % watchersList.size()));
    }

    /**
     * Check if the operation can be completed, if not watch it based on the given watch keys
     * <p>
     * Note that a delayed operation can be watched on multiple keys. It is possible that
     * an operation is completed after it has been added to the watch list for some, but
     * not all of the keys. In this case, the operation is considered completed and won't
     * be added to the watch list of the remaining keys. The expiration reaper thread will
     * remove this operation from any watcher list in which the operation exists.
     *
     * @param operation the delayed operation to be checked
     * @param watchKeys keys for bookkeeping the operation
     * @return true iff the delayed operations can be completed by the caller
     */
    public boolean tryCompleteElseWatch(T operation, Set<Object> watchKeys) {
        Preconditions.checkArgument(watchKeys.size() > 0, "The watch key list can't be empty");
        // The cost of tryComplete() is typically proportional to the number of keys. Calling
        // tryComplete() for each key is going to be expensive if there are many keys. Instead,
        // we do the check in the following way. Call tryComplete(). If the operation is not completed,
        // we just add the operation to all keys. Then we call tryComplete() again. At this time, if
        // the operation is still not completed, we are guaranteed that it won't miss any future triggering
        // event since the operation is already on the watcher list for all keys. This does mean that
        // if the operation is completed (by another thread) between the two tryComplete() calls, the
        // operation is unnecessarily added for watch. However, this is a less severe issue since the
        // expire reaper will clean it up periodically.

        boolean isCompletedByMe = operation.tryComplete();
        if (isCompletedByMe) {
            return true;
        }

        boolean watchCreated = false;
        for (Object key : watchKeys) {
            // If the operation is already completed, stop adding it to the rest of the watcher list.
            if (operation.isCompleted()) {
                return false;
            }
            watchForOperation(key, operation);
            if (!watchCreated) {
                watchCreated = true;
                estimatedTotalOperations.incrementAndGet();
            }
        }

        isCompletedByMe = operation.maybeTryComplete();
        if (isCompletedByMe) {
            return true;
        }

        // if it cannot be completed by now and hence is watched, add to the expire queue also
        if (!operation.isCompleted()) {
            timeoutTimer.add(operation);
            if (operation.isCompleted()) {
                // cancel the timer task
                operation.cancel();
            }
        }
        return false;
    }

    /**
     * Check if some some delayed operations can be completed with the given watch key,
     * and if yes complete them.
     *
     * @return the number of completed operations during this process
     */
    public int checkAndComplete(Object key) {
        Watchers watchers = null;
        WatchersList watchersList = selectWatchersList(key);
        watchersList.lock();
        try {
            watchers = watchersList.getWatchers(key);
        } finally {
            watchersList.unlock();
        }
        if (watchers == null) {
            return 0;
        } else {
            return watchers.tryCompleteWatched();
        }
    }

    /**
     * Return the number of delayed operations in the expiry queue
     */
    private int delayed() {
        return timeoutTimer.size();
    }

    /*
     * Return the watch list of the given key, note that we need to
     * grab the removeWatchersLock to avoid the operation being added to a removed watcher list
     */
    private void watchForOperation(Object key, T operation) {
        WatchersList watchersList = selectWatchersList(key);
        watchersList.lock();
        try {
            Watchers watcher = watchersList.getWatchers(key);
            if (watcher == null) {
                watcher = new Watchers(key);
                Watchers oldWatcher = watchersList.putIfAbsentWatchers(key, watcher);
                if (oldWatcher != null) {
                    watcher = oldWatcher;
                }
            }
            watcher.watch(operation);
        } finally {
            watchersList.unlock();
        }
    }

    /*
     * Remove the key from watcher lists if its list is empty
     */
    private void removeKeyIfEmpty(Object key, Watchers watchers) {
        WatchersList watchersList = selectWatchersList(key);
        watchersList.lock();
        try {
            // if the current key is no longer correlated to the watchers to remove, skip
            if (watchersList.getWatchers(key) == null || !watchersList.getWatchers(key).equals(watchers)) {
                return;
            }

            if (watchers != null && watchers.isEmpty()) {
                watchersList.removeWatchers(key);
            }
        } finally {
            watchersList.unlock();
        }
    }

    private void advanceClock(long timeoutMs) {
        timeoutTimer.advanceClock(timeoutMs);

        // Trigger a purge if the number of completed but still being watched operations is larger than
        // the purge threshold. That number is computed by the difference btw the estimated total number of
        // operations and the number of pending delayed operations.
        if (estimatedTotalOperations.get() - delayed() > purgeInterval) {
            // now set estimatedTotalOperations to delayed (the number of pending operations) since we are going to
            // clean up watchers. Note that, if more operations are completed during the clean up, we may end up with
            // a little overestimated total number of operations.
            estimatedTotalOperations.getAndSet(delayed());
//            logger.debug("Begin purging watch lists");

            int sum = 0;
            for (WatchersList watcherList : watchersList) {
                for (Watchers watchers : watcherList.allWatchers()) {
                    sum += watchers.purgeCompleted();
                }
            }
//            if (logger.isDebugEnabled()) {
//                logger.debug(String.format("Purged %d elements from watch lists.", sum));
//            }
        }
    }

    private class WatchersList {

        private ReentrantLock lock = new ReentrantLock();
        private ConcurrentMap<Object, Watchers> watchersForKey = new ConcurrentHashMap<Object, Watchers>();

        public void lock() {
            lock.lock();
        }

        public void unlock() {
            lock.unlock();
        }

        public Watchers getWatchers(Object key) {
            return watchersForKey.get(key);
        }

        public Watchers removeWatchers(Object key) {
            return watchersForKey.remove(key);
        }

        public Watchers putIfAbsentWatchers(Object key, Watchers watchers) {
            return watchersForKey.putIfAbsent(key, watchers);
        }

        public List<Watchers> allWatchers() {
            return Lists.newArrayList(watchersForKey.values());
        }
    }

    /**
     * A linked list of watched delayed operations based on some key
     */
    private class Watchers {

        private Object key;

        private Watchers(Object key) {
            this.key = key;
        }

        private ConcurrentLinkedQueue<T> operations = new ConcurrentLinkedQueue<T>();

        // count the current number of watched operations. This is O(n), so use isEmpty() if possible
        private int countWatched() {
            return operations.size();
        }

        private boolean isEmpty() {
            return operations.isEmpty();
        }

        // add the element to watch
        private void watch(T t) {
            operations.add(t);
        }

        // traverse the list and try to complete some watched elements
        private int tryCompleteWatched() {
            int completed = 0;

            Iterator<T> iter = operations.iterator();
            while (iter.hasNext()) {
                T curr = iter.next();
                if (curr.isCompleted()) {
                    // another thread has completed this operation, just remove it
                    iter.remove();
                } else if (curr.maybeTryComplete()) {
                    iter.remove();
                    completed += 1;
                }
            }

            if (operations.isEmpty()) {
                removeKeyIfEmpty(key, this);
            }

            return completed;
        }

        // traverse the list and purge elements that are already completed by others
        private int purgeCompleted() {
            int purged = 0;
            Iterator<T> iter = operations.iterator();
            while (iter.hasNext()) {
                T curr = iter.next();
                if (curr.isCompleted()) {
                    iter.remove();
                    purged += 1;
                }
            }

            if (operations.isEmpty()) {
                removeKeyIfEmpty(key, this);
            }
            return purged;

        }
    }


    /**
     * A background reaper to expire delayed operations that have timed out
     */
    private class ExpiredOperationReaper extends Thread {

        private String name;
        private boolean isInterruptible;
        private AtomicBoolean isRunning = new AtomicBoolean(true);
        private CountDownLatch shutdownLatch = new CountDownLatch(1);

        private ExpiredOperationReaper(String expirationReaper) {
            this(expirationReaper, false);
        }

        private ExpiredOperationReaper(String name, boolean isInterruptible) {
            super.setDaemon(false);
            super.setName(name);
            this.name = name;
            this.isInterruptible = isInterruptible;
        }

        public void shutdown() {
            initiateShutdown();
            awaitShutdown();
        }

        private boolean initiateShutdown() {
            if (isRunning.compareAndSet(true, false)) {
//                logger.info("Shutting down");
                isRunning.set(false);
                if (isInterruptible) {
                    interrupt();
                }
                return true;
            } else {
                return false;
            }
        }

        /**
         * After calling initiateShutdown(), use this API to wait until the shutdown is complete
         */
        private void awaitShutdown() {
            try {
                shutdownLatch.await();
            } catch (InterruptedException e) {
//                logger.error("thread interrupted");
            }
//            logger.info("Shutdown completed");
        }

        /**
         * This method is repeatedly invoked until the thread shuts down or this method throws an exception
         */
        public void doWork() {
            advanceClock(200L);
        }

        @Override
        public void run() {
//            logger.info("Starting " + purgatoryName);
            try {
                while (isRunning.get()) {
                    doWork();
                }
            } catch (Exception e) {
                if (isRunning.get()) {
//                    logger.error("Error due to ", e);
                }
            }
            shutdownLatch.countDown();
//            logger.info("Stopped ");
        }
    }
}
