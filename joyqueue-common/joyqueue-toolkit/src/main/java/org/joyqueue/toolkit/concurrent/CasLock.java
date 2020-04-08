package org.joyqueue.toolkit.concurrent;

import java.util.ConcurrentModificationException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于CAS实现的，轻量级的，可重入锁。
 * 只适用于锁碰撞非常罕见的场景。
 * @author LiYue
 * Date: 2020/4/2
 */
public class CasLock {
    private final static long FREE = -1L;
    private final AtomicLong lockThread = new AtomicLong(FREE);
    private final AtomicLong references = new AtomicLong(0L);

    /**
     * 获取锁，如果当前锁不可用，则等待直到可用后返回。
     * 慎用此方法！
     * 如果锁碰撞频率较高，此方法会大量占用CPU资源。
     */
    public void waitAndLock() {
        while (!tryLock()) {
            Thread.yield();
        }
    }

    /**
     * 检查锁是否可用，可用则获取锁并返回。
     * @throws ConcurrentModificationException 锁不可用时抛出此异常。
     */
    public void checkLock() {

        long thread = Thread.currentThread().getId();
        if (thread != lockThread.get() && !lockThread.compareAndSet(FREE, thread)) {
            throw new ConcurrentModificationException();
        }
        references.getAndIncrement();
    }

    /**
     * 检查锁是否可用，可用则获取锁并返回true, 否则返回false。
     * @return 可用则获取锁并返回true, 否则返回false。
     */
    public boolean tryLock() {
        long thread = Thread.currentThread().getId();
        if (thread != lockThread.get() && !lockThread.compareAndSet(FREE, thread)) {
            return false;
        }
        references.getAndIncrement();
        return true;
    }
    /**
     * 释放锁。
     */
    public void unlock() {
        long thread = Thread.currentThread().getId();
        if (thread == lockThread.get() && references.decrementAndGet() == 0) {
            lockThread.set(FREE);
        }
    }
}
