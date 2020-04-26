package org.joyqueue.toolkit.concurrent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于CAS和引用计数实现的，高性能、不可重入、读写锁。
 *
 * 思路是用一个原子变量refCnt记录锁状态：
 * 1. 大于0（FREE），为读锁状态，可以一个或任意多个持有者持有读锁。
 * 2. 等于0 (FREE)，为空闲状态。
 * 3. 等于-1（LOCK_WRITE），为写锁状态，写锁只能有一个持有者，且和所有读锁持有者互斥。
 *
 * 另外，在等待锁时，优化了CPU占用。
 *
 * 注意：
 *
 * 1. 该锁为不可重入锁；
 * 2. 为了提升性能，锁的实现没有任何安全检查。
 *
 */
public class CasReadWriteLock {
    private static final long FREE = 0L;
    private static final long LOCK_WRITE = -1L;
    private final CasLock upgradeLock = new CasLock();
    // 引用计数
    // 大于 FREE：读锁中
    // FREE: 资源可用
    // LOCK_WRITE: 写锁中
    private final AtomicLong refCnt = new AtomicLong(FREE);

    /**
     * 反复自旋
     */
    public void lockRead() {
        int yieldCount = 0;
        while (!tryLockRead()) {
            yieldCount = yieldCount > 50 ? yieldCount - 50 : yieldCount;

            if(yieldCount == 50 - 1) {
                sleepQuietly(1);
            } else if (yieldCount % 20 == 20 - 1) {
                sleepQuietly(0);
            } else {
                Thread.yield();

            }
            yieldCount ++;
        }
    }

    public boolean tryLockRead() {
        long ref;
        return !upgradeLock.isLocked() && (ref = refCnt.get()) >= FREE && refCnt.compareAndSet(ref, ref + 1);
    }

    public void lockWrite() {
        int yieldCount = 0;
        while (!tryLockWrite()) {
            yieldCount = yieldCount > 50 ? yieldCount - 50 : yieldCount;

            if(yieldCount == 50 - 1) {
                sleepQuietly(1);
            } else if (yieldCount % 20 == 20 - 1) {
                sleepQuietly(0);
            } else {
                Thread.yield();

            }
            yieldCount ++;
        }
    }

    private void upgrade() {
        int yieldCount = 0;
        while (!refCnt.compareAndSet(1, LOCK_WRITE)) {
            yieldCount = yieldCount > 50 ? yieldCount - 50 : yieldCount;

            if(yieldCount == 50 - 1) {
                sleepQuietly(1);
            } else if (yieldCount % 20 == 20 - 1) {
                sleepQuietly(0);
            } else {
                Thread.yield();

            }
            yieldCount ++;
        }
    }

    public boolean tryUpgradeToWriteLock() {
        if (upgradeLock.tryLock()) {
            try {
                upgrade();
                return true;
            } finally {
                upgradeLock.unlock();
            }
        }
        return false;
    }

    public boolean tryLockWrite() {
        return refCnt.compareAndSet(FREE, LOCK_WRITE);
    }

    private void sleepQuietly(int i){
        try {
            Thread.sleep(i);
        } catch (InterruptedException ignored) {
        }
    }

    public void unlockRead() {
        refCnt.getAndDecrement();
    }

    public void unlockWrite() {
        refCnt.compareAndSet(LOCK_WRITE, FREE);
    }

    public void downgradeToReadLock() {
        refCnt.compareAndSet(LOCK_WRITE, 1);
    }
}
