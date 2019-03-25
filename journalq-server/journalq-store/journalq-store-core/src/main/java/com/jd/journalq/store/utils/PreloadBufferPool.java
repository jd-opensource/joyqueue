package com.jd.journalq.store.utils;

import com.jd.journalq.toolkit.concurrent.LoopThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Cleaner;
import sun.misc.VM;
import sun.nio.ch.DirectBuffer;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author liyue25
 * Date: 2018-12-20
 */
public class PreloadBufferPool {
    private static final Logger logger = LoggerFactory.getLogger(PreloadBufferPool.class);
    private Map<Integer,PreLoadCache> bufferCache = new ConcurrentHashMap<>();
    private final LoopThread loopThread;
    private final LoopThread printThread;

    // 缓存比率： 如果非堆内存使用率超过这个比率，就不再申请内存，抛出OOM。
    // 由于jvm在读写文件的时候会用到少量DirectBuffer作为缓存，必须预留一部分。
    private final static float CACHE_RATIO = 0.9f;
    private final AtomicLong usedSize = new AtomicLong(0L);
    public PreloadBufferPool(long intervalMs) {
        this(intervalMs, 0L);
    }
    public PreloadBufferPool(long intervalMs, long dmuInterval) {
        loopThread = LoopThread.builder()
                .name("PreloadBufferPoolThread")
                .sleepTime(intervalMs, intervalMs)
                .doWork(this::preLoadBuffer)
                .onException(e -> logger.warn("PreloadBufferPoolThread exception:", e))
                .daemon(true)
                .build();
        loopThread.start();

        if(dmuInterval > 0) {
            printThread = LoopThread.builder()
                    .name("DirectBufferPrintThread")
                    .sleepTime(dmuInterval, dmuInterval)
                    .doWork(() -> {
                        long used = usedSize.get();
                        long plUsed = bufferCache.values().stream().mapToLong(preLoadCache -> {
                            long cached = preLoadCache.cache.size();
                            long fly = preLoadCache.onFlyCounter.get();
                            long totalSize = preLoadCache.bufferSize * (cached + fly);
                            logger.info("PreloadCache usage: cached: {} * {} = {}, onFly: {} * {} = {}, total: {}",
                                    preLoadCache.bufferSize, cached, String.format("%.2f MB", (double) preLoadCache.bufferSize * cached / 1024 / 1024),
                                    preLoadCache.bufferSize, fly, String.format("%.2f MB", (double) preLoadCache.bufferSize * fly / 1024 / 1024),
                                    String.format("%.2f MB", (double) totalSize / 1024 / 1024));
                            return totalSize;
                        }).sum();
                        logger.info("DirectBuffer usage: {}.", String.format("%.2f MB/%.2f MB/%.2f MB", plUsed * 1D / 1024 /1024, used * 1D / 1024 / 1024, VM.maxDirectMemory() * 1D / 1024 / 1024));

                    })
                    .daemon(true)
                    .build();
            printThread.start();
        } else {
            printThread = null;
        }
    }

    public synchronized boolean addPreLoad(int bufferSize, int coreCount, int maxCount) {
        return bufferCache.putIfAbsent(bufferSize, new PreLoadCache(bufferSize, coreCount, maxCount)) == null;
    }

    public void close() {
        this.loopThread.stop();
        if(this.printThread != null) {
            this.printThread.stop();
        }
        bufferCache.values().forEach(p -> {
            while (!p.cache.isEmpty()) {
                destroyOne(p.cache.remove());

            }
        });
    }

    private void destroyOne(ByteBuffer byteBuffer) {
//        logger.info("Release: {}", byteBuffer.capacity());
        usedSize.getAndAdd(-1 * byteBuffer.capacity());
        releaseIfDirect(byteBuffer);
    }

    private void preLoadBuffer() {
        for(PreLoadCache preLoadCache: bufferCache.values()) {
            while (preLoadCache.cache.size() > preLoadCache.maxCount) {
                try {
                    destroyOne(preLoadCache.cache.remove());
                } catch (NoSuchElementException ignored) {}
            }
        }
        for(PreLoadCache preLoadCache: bufferCache.values()) {
            try {
                while (preLoadCache.cache.size() < preLoadCache.coreCount) {
                    preLoadCache.cache.add(createOne(preLoadCache.bufferSize));
                }
            } catch (OutOfMemoryError ignored) {}
        }
    }

    private ByteBuffer createOne(int size) {
        while (usedSize.get() + size > VM.maxDirectMemory() * CACHE_RATIO) {
            PreLoadCache preLoadCache = bufferCache.values().stream()
                    .filter(p -> p.cache.size() > 0)
                    .findAny().orElse(null);
            if(null != preLoadCache) {
                destroyOne(preLoadCache.cache.remove());
            } else {
                break;
            }
        }

        if(usedSize.get() + size > VM.maxDirectMemory() * CACHE_RATIO)
            throw new OutOfMemoryError();
        long u;
        while (!usedSize.compareAndSet(u = usedSize.get(), u + size)){
            Thread.yield();
        }
//        logger.info("Allocate : {}", size);
        return ByteBuffer.allocateDirect(size);

    }

    private void releaseIfDirect(ByteBuffer byteBuffer) {
        if(byteBuffer instanceof DirectBuffer) {
            try {
                Method getCleanerMethod;
                getCleanerMethod = byteBuffer.getClass().getMethod("cleaner");
                getCleanerMethod.setAccessible(true);
                Cleaner cleaner = (Cleaner) getCleanerMethod.invoke(byteBuffer, new Object[0]);
                cleaner.clean();
            }catch (Exception e) {
                logger.warn("Exception: ", e);
            }
        }
    }


    public ByteBuffer allocate(int bufferSize) {
        try {
            PreLoadCache preLoadCache = bufferCache.get(bufferSize);
            if(null != preLoadCache) {
                try {
                    return preLoadCache.cache.remove();
                } catch (NoSuchElementException e) {
                    logger.debug("Pool is empty, create ByteBuffer: {}", bufferSize);
                    return createOne(bufferSize);
                } finally {
                    preLoadCache.onFlyCounter.getAndIncrement();
                }
            } else {
                logger.warn("No cached buffer in pool, create ByteBuffer: {}", bufferSize);
                return createOne(bufferSize);

            }
        } catch (OutOfMemoryError outOfMemoryError) {
            logger.debug("OOM: {}.", String.format("%.2f MB/%.2f MB", usedSize.get() * 1D / 1024 / 1024, VM.maxDirectMemory() * 1D / 1024 / 1024));
            throw outOfMemoryError;
        }
    }
    public void release(ByteBuffer byteBuffer) {
        int size = byteBuffer.capacity();
        PreLoadCache preLoadCache = bufferCache.get(size);
        if(null != preLoadCache) {
            byteBuffer.clear();
            preLoadCache.cache.add(byteBuffer);
            preLoadCache.onFlyCounter.getAndDecrement();
        } else {
            destroyOne(byteBuffer);
        }
    }

    public long maxMemorySize() {
        return bufferCache.entrySet().stream().mapToLong(entry -> entry.getKey() * entry.getValue().maxCount).sum();
    }


    static class PreLoadCache {
        final int bufferSize;
        final int coreCount, maxCount;
        final Queue<ByteBuffer> cache = new ConcurrentLinkedQueue<>();
        final AtomicLong onFlyCounter = new AtomicLong(0L);
        PreLoadCache(int bufferSize, int coreCount, int maxCount) {
            this.bufferSize = bufferSize;
            this.coreCount = coreCount;
            this.maxCount = maxCount;
        }
    }
}
