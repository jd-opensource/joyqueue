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
package com.jd.journalq.store.utils;

import com.jd.journalq.toolkit.concurrent.LoopThread;
import com.jd.journalq.toolkit.format.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Cleaner;
import sun.misc.VM;
import sun.nio.ch.DirectBuffer;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author liyue25
 * Date: 2018-12-20
 */
public class PreloadBufferPool implements Closeable {
    public static final long DEFAULT_CACHE_LIFE_TIME_MS = 60000L;
    public static final long INTERVAL_MS = 50L;
    private static final Logger logger = LoggerFactory.getLogger(PreloadBufferPool.class);
    // 缓存比率：如果非堆内存使用率超过这个比率，就不再申请内存，抛出OOM。
    // 由于jvm在读写文件的时候会用到少量DirectBuffer作为缓存，必须预留一部分。
    private static final double CACHE_RATIO = 0.9d;
    /**
     * 缓存清理比率阈值，超过这个阈值执行缓存清理。
     */
    private static final double EVICT_RATIO = 0.8d;
    private final LoopThread preloadThread;
    private final LoopThread metricThread;
    private final LoopThread evictThread;
    private final long cacheLifetimeMs;
    private final long maxMemorySize;
    private final AtomicLong usedSize = new AtomicLong(0L);
    private final Set<BufferHolder> directBufferHolders = ConcurrentHashMap.newKeySet();
    private final Set<BufferHolder> mMapBufferHolders = ConcurrentHashMap.newKeySet();
    private Map<Integer, PreLoadCache> bufferCache = new ConcurrentHashMap<>();

    public PreloadBufferPool() {
        this(0L, DEFAULT_CACHE_LIFE_TIME_MS, Math.round(VM.maxDirectMemory() * CACHE_RATIO));
    }

    public PreloadBufferPool(long printMetricIntervalMs) {
        this(printMetricIntervalMs, DEFAULT_CACHE_LIFE_TIME_MS, Math.round(VM.maxDirectMemory() * CACHE_RATIO));
    }

    public PreloadBufferPool(long printMetricInterval, long cacheLifetimeMs, long maxMemorySize) {
        this.cacheLifetimeMs = cacheLifetimeMs;
        preloadThread = buildPreloadThread();
        preloadThread.start();

        if (printMetricInterval > 0) {
            metricThread = buildMetricThread(printMetricInterval);
            metricThread.start();
        } else {
            metricThread = null;
        }

        evictThread = buildEvictThread();
        evictThread.start();
        this.maxMemorySize = maxMemorySize;
        logger.info("Max direct memory size : {}.", Format.formatTraffic(maxMemorySize));
    }

    private LoopThread buildMetricThread(long printMetricInterval) {
        return LoopThread.builder()
                .name("DirectBufferPrintThread")
                .sleepTime(printMetricInterval, printMetricInterval)
                .doWork(() -> {
                    long used = usedSize.get();
                    long plUsed = bufferCache.values().stream().mapToLong(preLoadCache -> {
                        long cached = preLoadCache.cache.size();
                        long usedPreLoad = preLoadCache.onFlyCounter.get();
                        long totalSize = preLoadCache.bufferSize * (cached + usedPreLoad);
                        logger.info("PreloadCache usage: cached: {} * {} = {}, used: {} * {} = {}, total: {}",
                                Format.formatTraffic(preLoadCache.bufferSize), cached, Format.formatTraffic(preLoadCache.bufferSize * cached),
                                Format.formatTraffic(preLoadCache.bufferSize), usedPreLoad, Format.formatTraffic(preLoadCache.bufferSize * usedPreLoad),
                                Format.formatTraffic(totalSize));
                        return totalSize;
                    }).sum();
                    logger.info("DirectBuffer preload/used/max: {}/{}/{}.",
                            Format.formatTraffic(plUsed),
                            Format.formatTraffic(used),
                            Format.formatTraffic(maxMemorySize));

                })
                .daemon(true)
                .build();
    }

    private LoopThread buildPreloadThread() {
        return LoopThread.builder()
                .name("PreloadBufferPoolThread")
                .sleepTime(INTERVAL_MS, INTERVAL_MS)
                .doWork(this::preLoadBuffer)
                .onException(e -> logger.warn("PreloadBufferPoolThread exception:", e))
                .daemon(true)
                .build();
    }

    private LoopThread buildEvictThread() {
        return LoopThread.builder()
                .name("EvictThread")
                .sleepTime(INTERVAL_MS, INTERVAL_MS)
                .condition(() -> usedSize.get() > maxMemorySize * EVICT_RATIO)
                .doWork(this::evict)
                .onException(e -> logger.warn("EvictThread exception:", e))
                .daemon(true)
                .build();
    }


    /**
     * 清除文件缓存页。LRU。
     */
    private void evict() {
        // 先清除过期的
        for (BufferHolder holder : directBufferHolders) {
            if (System.currentTimeMillis() - holder.lastAccessTime() > cacheLifetimeMs) {
                holder.evict();
            }
        }

        mMapBufferHolders.removeIf(holder -> System.currentTimeMillis() - holder.lastAccessTime() > cacheLifetimeMs && holder.evict());


        // 清理超过maxCount的缓存页
        for (PreLoadCache preLoadCache : bufferCache.values()) {
            while (preLoadCache.cache.size() > preLoadCache.maxCount) {
                if (usedSize.get() < maxMemorySize * EVICT_RATIO) {
                    return;
                }
                try {
                    destroyOne(preLoadCache.cache.remove());
                } catch (NoSuchElementException ignored) {
                }
            }
        }

        // 清理使用中最旧的页面，直到内存占用率达标

        if (usedSize.get() > maxMemorySize * EVICT_RATIO) {
            List<LruWrapper<BufferHolder>> sorted;
            sorted = directBufferHolders.stream()
                    .filter(BufferHolder::isFree)
                    .map(bufferHolder -> new LruWrapper<>(bufferHolder, bufferHolder.lastAccessTime()))
                    .sorted(Comparator.comparing(LruWrapper::getLastAccessTime))
                    .collect(Collectors.toList());

            while (usedSize.get() > maxMemorySize * EVICT_RATIO && !sorted.isEmpty()) {
                LruWrapper<BufferHolder> wrapper = sorted.remove(0);
                BufferHolder holder = wrapper.get();
                if (holder.lastAccessTime() == wrapper.getLastAccessTime()) {
                    holder.evict();
                }
            }
        }


    }

    public synchronized boolean addPreLoad(int bufferSize, int coreCount, int maxCount) {
        return bufferCache.putIfAbsent(bufferSize, new PreLoadCache(bufferSize, coreCount, maxCount)) == null;
    }

    public void close() {
        this.preloadThread.stop();
        this.evictThread.stop();
        if (this.metricThread != null) {
            this.metricThread.stop();
        }
        bufferCache.values().forEach(p -> {
            while (!p.cache.isEmpty()) {
                destroyOne(p.cache.remove());

            }
        });
    }

    private void destroyOne(ByteBuffer byteBuffer) {
        usedSize.getAndAdd(-1 * byteBuffer.capacity());
        releaseIfDirect(byteBuffer);
    }

    private void preLoadBuffer() {

        for (PreLoadCache preLoadCache : bufferCache.values()) {
            try {
                while (preLoadCache.cache.size() < preLoadCache.coreCount && usedSize.get() + preLoadCache.bufferSize < maxMemorySize) {
                    preLoadCache.cache.add(createOne(preLoadCache.bufferSize));
                }
            } catch (OutOfMemoryError ignored) {
            }
        }
    }

    private ByteBuffer createOne(int size) {
        while (usedSize.get() + size > maxMemorySize) {
            PreLoadCache preLoadCache = bufferCache.values().stream()
                    .filter(p -> p.cache.size() > 0)
                    .findAny().orElse(null);
            if (null != preLoadCache) {
                destroyOne(preLoadCache.cache.remove());
            } else {
                break;
            }
        }

        if (usedSize.get() + size > maxMemorySize) {
            // 如果内存不足，唤醒清理线程立即执行清理
            evictThread.weakup();
            // 等待5x10ms，如果还不足抛出异常
            for (int i = 0; i < 5 && usedSize.get() + size > maxMemorySize; i++) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted: ", e);
                }
            }
            if (usedSize.get() + size > maxMemorySize) {
                throw new OutOfMemoryError();
            }
        }
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size);
        long u;
        while (!usedSize.compareAndSet(u = usedSize.get(), u + size)) {
            Thread.yield();
        }

        if (usedSize.get() > maxMemorySize * EVICT_RATIO) {
            evictThread.weakup();
        }
//        logger.info("Allocate : {}", size);
        return byteBuffer;

    }

    private void releaseIfDirect(ByteBuffer byteBuffer) {
        if (byteBuffer instanceof DirectBuffer) {
            try {
                Method getCleanerMethod;
                getCleanerMethod = byteBuffer.getClass().getMethod("cleaner");
                getCleanerMethod.setAccessible(true);
                Cleaner cleaner = (Cleaner) getCleanerMethod.invoke(byteBuffer, new Object[0]);
                cleaner.clean();
            } catch (Exception e) {
                logger.warn("Exception: ", e);
            }
        }
    }

    public void addMemoryMappedBufferHolder(BufferHolder bufferHolder) {
        mMapBufferHolders.add(bufferHolder);
    }

    public ByteBuffer allocate(int bufferSize, BufferHolder bufferHolder) {
        ByteBuffer buffer = allocate(bufferSize);
        directBufferHolders.add(bufferHolder);
        return buffer;
    }

    private ByteBuffer allocate(int bufferSize) {
        try {
            PreLoadCache preLoadCache = bufferCache.get(bufferSize);
            if (null != preLoadCache) {
                try {
                    ByteBuffer byteBuffer = preLoadCache.cache.remove();
                    preLoadCache.onFlyCounter.getAndIncrement();
                    return byteBuffer;
                } catch (NoSuchElementException e) {
                    logger.debug("Pool is empty, create ByteBuffer: {}", bufferSize);
                    ByteBuffer byteBuffer = createOne(bufferSize);
                    preLoadCache.onFlyCounter.getAndIncrement();
                    return byteBuffer;
                }
            } else {
                logger.warn("No cached buffer in pool, create ByteBuffer: {}", bufferSize);
                return createOne(bufferSize);

            }
        } catch (OutOfMemoryError outOfMemoryError) {
            logger.debug("OOM: {}/{}.", Format.formatTraffic(usedSize.get()), Format.formatTraffic(maxMemorySize));
            throw outOfMemoryError;
        }
    }

    public void release(ByteBuffer byteBuffer, BufferHolder bufferHolder) {
        directBufferHolders.remove(bufferHolder);
        int size = byteBuffer.capacity();
        PreLoadCache preLoadCache = bufferCache.get(size);
        if (null != preLoadCache) {
            byteBuffer.clear();
            preLoadCache.cache.add(byteBuffer);
            preLoadCache.onFlyCounter.getAndDecrement();
        } else {
            destroyOne(byteBuffer);
        }
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

    private static class LruWrapper<V> {
        private final long lastAccessTime;
        private final V t;

        LruWrapper(V t, long lastAccessTime) {
            this.lastAccessTime = lastAccessTime;
            this.t = t;
        }

        private long getLastAccessTime() {
            return lastAccessTime;
        }

        private V get() {
            return t;
        }
    }
}
