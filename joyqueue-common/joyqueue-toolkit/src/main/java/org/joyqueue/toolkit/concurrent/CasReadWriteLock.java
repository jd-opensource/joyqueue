package org.joyqueue.toolkit.concurrent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于Cas高性能不可重入读写锁。
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

    private boolean canUpgrade() {
        long ref;
        return (ref = refCnt.get()) >= 1 && refCnt.compareAndSet(ref, ref + 1);
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

}
