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
package com.jd.journalq.registry.zookeeper.manager;

import com.jd.journalq.registry.listener.ConnectionEvent;
import com.jd.journalq.registry.listener.ConnectionListener;
import com.jd.journalq.registry.util.Path;
import com.jd.journalq.registry.zookeeper.ZKClient;
import com.jd.journalq.toolkit.concurrent.EventBus;
import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.toolkit.concurrent.Locks;
import com.google.common.base.Charsets;
import com.jd.journalq.toolkit.network.IpUtil;
import com.jd.journalq.toolkit.os.Systems;
import com.jd.journalq.toolkit.service.Service;
import com.jd.journalq.toolkit.time.SystemClock;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 分布式锁管理，临时节点的需要有限，Integer.MAX_VALUE
 *
 * @author hexiaofeng
 * @version 1.0.0
 * @since 12-12-19 上午9:29
 */
public class LockManager extends Service {
    // Zookeeper客户端
    protected ZKClient zkClient;
    // 存活的节点
    protected ConcurrentMap<String, ZKLock> locks = new ConcurrentHashMap<String, ZKLock>();
    // 待删除节点
    protected EventBus<String> removes = new EventBus<String>("unlock", new DeleteTask());
    // 连接监听器
    protected ConnectionListener connectionListener = new ConnectionWatcher();
    // 身份信息
    protected String identity;

    public LockManager(final ZKClient zkClient) {
        if (zkClient == null) {
            throw new IllegalArgumentException("zkClient is null");
        }
        this.zkClient = zkClient;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        identity = IpUtil.getLocalIp() + ":" + Systems.getPid();
        removes.start();
        zkClient.addListener(connectionListener);
    }

    @Override
    protected void doStop() {
        zkClient.removeListener(connectionListener);
        // 尝试释放锁，有可能现在锁还被使用中
        for (Map.Entry<String, ZKLock> entry : locks.entrySet()) {
            entry.getValue().stop();
        }
        // DeleteTask不能加锁，否则会死锁
        removes.stop(true);
        locks.clear();
        super.doStop();
    }

    /**
     * 创建分布式锁<br>
     * 返回的锁在调用lock方法会抛出IllegalStateException<br>
     * 分布式锁在使用过程中可能会由于网络原因，造成其它候选人拿到锁<br>
     * 所以，在锁中的执行代码要尽可能快的执行完。
     *
     * @param path 路径
     * @return 锁
     */
    public Lock createLock(final String path) {
        Path.validate(path);
        // 防止在终止
        readLock.lock();
        try {
            if (!isStarted()) {
                throw new IllegalStateException("lock manager is not started.");
            }
            ZKLock lock = locks.get(path);
            if (lock == null) {
                lock = new ZKLock(path);
                ZKLock old = locks.putIfAbsent(path, lock);
                if (old != null) {
                    lock = old;
                }
            }
            // 增加引用计数器
            lock.counter.incrementAndGet();
            return lock;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 基于Zookeeper的分布式锁，支持多线程，多进程，可重入
     */
    protected class ZKLock implements Lock {
        // 父节点路径
        protected String parent;
        // 完整路径
        protected String path;
        // 节点名称
        protected String name;
        // 正在监视的节点名称
        protected String watching;
        // 锁状态
        protected AtomicReference<LockState> state = new AtomicReference<LockState>(LockState.NONE);
        // JVM内存锁，采用2阶段锁
        protected ReentrantLock lock = new ReentrantLock();
        // 引用计数器
        protected AtomicLong counter = new AtomicLong(0);
        // 等待Leader的信号量
        protected final Object mutex = new Object();
        // 孩子节点监听器
        protected final Watcher watcher = new Watcher() {
            @Override
            public void process(final WatchedEvent event) {
                // None类型会在Session expired / connection loss/  auth failed得到对应的触发
                // 针对出现None的类型，会将所有的watcher进行触发，同时并不会移除watcher
                // 所以，watcher会在下一次reconnect成功后再次触发，除非设置DisableAutoResetWatch

                // 检查状态
                switch (state.get()) {
                    case STOPPED:
                        return;
                    case LOCKED:
                        return;
                    case NONE:
                        return;
                    case LOCKING:
                        if (event.getType() == Event.EventType.None) {
                            // 重新选举
                            state.compareAndSet(LockState.LOCKING, LockState.NONE);
                            // 删除原有节点
                            removes.add(path);
                        } else if (Path.node(event.getPath()).equals(watching)) {
                            // 是监视节点的事件，防止重连后，原来的节点重新触发的事件
                            try {
                                if (elect(name)) {
                                    mutex.notifyAll();
                                }
                            } catch (Exception e) {
                                // 重新选举
                                state.compareAndSet(LockState.LOCKING, LockState.NONE);
                                // 删除原有节点
                                removes.add(path);
                            }
                        }
                }
            }
        };

        public ZKLock(String parent) {
            this.parent = parent;
        }

        /**
         * 释放
         */
        protected void stop() {
            unlock();
            state.set(LockState.STOPPED);
        }

        /**
         * 状态转换
         *
         * @return true 如果是领导
         */
        protected boolean transit() {
            switch (state.get()) {
                case LOCKING:
                    exists();
                    return false;
                case LOCKED:
                    return true;
                case STOPPED:
                    throw new IllegalStateException("lock manager is stopped.");
                case NONE:
                    return vote();
            }
            return false;
        }

        /**
         * 开始投票
         *
         * @return true 如果是领导
         */
        protected boolean vote() {
            String node = null;
            // 状态转换
            if (state.compareAndSet(LockState.NONE, LockState.LOCKING)) {
                try {
                    watching = null;
                    name = null;
                    path = null;
                    // 创建临时节点
                    node = zkClient.create(Path.concat(parent, "lock-"), identity.getBytes(Charsets.UTF_8),
                            CreateMode.EPHEMERAL_SEQUENTIAL);
                    path = node;
                    name = path.substring(parent.length() + 1);
                    // 判断该节点是否是Leader
                    if (elect(name)) {
                        state.set(LockState.LOCKED);
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    state.compareAndSet(LockState.LOCKING, LockState.NONE);
                    if (node != null) {
                        // 获取数据出错，没有监听上，则删除该节点，重新创建
                        removes.add(node);
                    }
                }
            }
            return false;
        }

        /**
         * 竞争阶段，判断选举节点是否存在
         */
        protected void exists() {
            String node = path;
            if (state.get() != LockState.LOCKING || node == null) {
                return;
            }
            try {
                // 检查一下节点是否还存在，如果不存在，可以重新注册
                if (!zkClient.exists(node)) {
                    state.compareAndSet(LockState.LOCKING, LockState.NONE);
                }
            } catch (Exception e) {
                // 忽略
            }
        }

        /**
         * 选举
         *
         * @param name 节点名称
         * @return
         * @throws Exception
         */
        protected boolean elect(final String name) throws Exception {
            // 获取节点数据
            List<String> children = zkClient.getSortedChildren(parent);
            if (name.equals(children.get(0))) {
                // 是Leader节点
                return true;
            } else {
                String child;
                // 监控该节点的前一个节点
                for (int i = 0; i < children.size(); i++) {
                    child = children.get(i);
                    if (child.equals(name)) {
                        watching = child;
                        // 存在监视
                        zkClient.exists(Path.concat(parent, children.get(i - 1)), watcher);
                        break;
                    }
                }
                return false;
            }
        }

        @Override
        public void lock() {
            // 先使用内部锁
            lock.lock();
            while (state.get() != LockState.LOCKED) {
                try {
                    // 判断节点是否创建成功
                    if (transit()) {
                        return;
                    } else {
                        // 没有创建成功，等待1秒后重建
                        Locks.awaitQuiet(mutex, 1000);
                    }
                } catch (RuntimeException e) {
                    // 捕获运行时异常，如果出错则是否内部锁
                    lock.unlock();
                    throw e;
                }
            }
        }


        @Override
        public void lockInterruptibly() throws InterruptedException {
            // 先使用内部锁
            lock.lockInterruptibly();
            try {
                while (state.get() != LockState.LOCKED) {
                    // 判断节点是否创建成功
                    if (transit()) {
                        // 已经是Leader
                        return;
                    } else {
                        // 等待一秒钟重新进行创建
                        Locks.await(mutex, 1000);
                    }
                }
            } catch (RuntimeException e) {
                // 捕获运行时异常，如果出错则是否内部锁
                lock.unlock();
                throw e;
            } catch (InterruptedException e) {
                // 捕获中断异常，如果出错则是否内部锁
                lock.unlock();
                Thread.currentThread().interrupt();
                throw e;
            }
        }

        @Override
        public boolean tryLock() {
            // 先使用内部锁
            if (lock.tryLock()) {
                try {
                    // 创建节点
                    return transit() && state.get() == LockState.LOCKED;
                } catch (RuntimeException e) {
                    // 捕获运行时异常，如果出错则是否内部锁
                    lock.unlock();
                    throw e;
                }
            }
            return false;
        }

        @Override
        public boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException {
            long start = SystemClock.now();
            long millis = unit.toMillis(time);
            long remain;
            // 先使用内部锁
            if (lock.tryLock(time, unit)) {
                while (state.get() != LockState.LOCKED && (SystemClock.now() - start <= millis)) {
                    try {
                        if (transit()) {
                            // 已经是Leader
                            return true;
                        } else {
                            // 等待一秒钟重新进行创建
                            remain = (millis - (SystemClock.now() - start));
                            if (remain > 0) {
                                Locks.await(mutex, remain > 1000 ? 1000 : remain);
                            } else {
                                return false;
                            }
                        }
                    } catch (InterruptedException e) {
                        lock.unlock();
                        Thread.currentThread().interrupt();
                        throw e;
                    } catch (RuntimeException e) {
                        // 捕获运行时异常，如果出错则是否内部锁
                        lock.unlock();
                        throw e;
                    }
                }
            }
            return false;
        }

        @Override
        public void unlock() {
            switch (state.get()) {
                case NONE:
                    break;
                case STOPPED:
                    break;
                case LOCKING:
                case LOCKED:
                    try {
                        // 判断引用计数器
                        long ref = counter.decrementAndGet();
                        if (ref == 0) {
                            // 从锁里面清理掉
                            locks.remove(parent);
                            // 放到队列里面去异步删除
                            removes.add(path);
                            state.set(LockState.NONE);
                        } else {
                            state.set(LockState.NONE);
                        }
                    } finally {
                        if (lock.isLocked()) {
                            lock.unlock();
                        }
                    }
            }
        }

        @Override
        public Condition newCondition() {
            return lock.newCondition();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ZKLock lock = (ZKLock) o;

            return parent.equals(lock.parent);

        }

        @Override
        public int hashCode() {
            return parent.hashCode();
        }
    }

    /**
     * 异步删除锁节点任务
     */
    protected class DeleteTask implements EventListener<String> {
        @Override
        public void onEvent(final String event) {
            if (event == null) {
                return;
            }
            try {
                zkClient.delete(event);
            } catch (Exception e) {
                if (isStarted()) {
                    // 出错，重新入队
                    removes.add(event);
                }
            }
        }
    }

    /**
     * 连接监听器
     */
    protected class ConnectionWatcher implements ConnectionListener {
        @Override
        public void onEvent(final ConnectionEvent event) {
            // 需要清理锁
            switch (event.getType()) {
                case LOST:
                case FAILED:
                    // 防止在停止
                    readLock.lock();
                    try {
                        if (!isStarted()) {
                            return;
                        }
                        for (Map.Entry<String, ZKLock> entry : locks.entrySet()) {
                            // 释放锁
                            entry.getValue().unlock();
                        }
                    } finally {
                        readLock.unlock();
                    }
            }
        }
    }

    /**
     * 连接的状态，判断是否可以创建锁
     */
    protected enum LockState {
        /**
         * 没有拥有锁
         */
        NONE,
        /**
         * 正在竞争锁
         */
        LOCKING,
        /**
         * 拥有锁
         */
        LOCKED,
        /**
         * 停止
         */
        STOPPED
    }
}
