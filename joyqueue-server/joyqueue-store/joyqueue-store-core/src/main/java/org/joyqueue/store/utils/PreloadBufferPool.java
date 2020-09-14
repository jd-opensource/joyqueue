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
package org.joyqueue.store.utils;

import org.joyqueue.monitor.BufferPoolMonitorInfo;
import org.joyqueue.toolkit.concurrent.LoopThread;
import org.joyqueue.toolkit.format.Format;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Cleaner;
import sun.misc.VM;
import sun.nio.ch.DirectBuffer;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author liyue25
 * Date: 2018-12-20
 */
public class PreloadBufferPool {
    private static final long INTERVAL_MS = 50L;
    private static final Logger logger = LoggerFactory.getLogger(PreloadBufferPool.class);
    // 缓存比率：如果非堆内存使用率超过这个比率，就不再申请内存，抛出OOM。
    // 由于jvm在读写文件的时候会用到少量DirectBuffer作为缓存，必须预留一部分。
    private static final double CACHE_RATIO = 0.9d;
    /**
     * 缓存清理比率阈值，超过这个阈值执行缓存清理。
     */
    private static final double EVICT_RATIO = 0.9d;
    /**
     * 缓存核心利用率，系统会尽量将这个比率以内的内存用满。
     */
    private static final double CORE_RATIO = 0.8d;

    private static final long DEFAULT_WRITE_PAGE_EXTRA_WEIGHT_MS = 60000L;
    public static final String PRINT_METRIC_INTERVAL_MS_KEY = "PreloadBufferPool.PrintMetricIntervalMs";
    public static final String MAX_MEMORY_KEY = "PreloadBufferPool.MaxMemory";
    private static final String WRITE_PAGE_EXTRA_WEIGHT_MS_KEY = "PreloadBufferPool.WritePageExtraWeightMs";
    private static final String MAX_PAGE_AGE = "PreloadBufferPool.MaxPageAge";
    private static final int DEFAULT_MAX_PAGE_AGE = 1000 * 60 * 5;
    private final LoopThread preloadThread;
    private final LoopThread metricThread;
    private final LoopThread evictThread;
    private final long maxMemorySize;
    private final long coreMemorySize;
    private final long evictMemorySize;
    private final int maxPageAge;

    // 正在写入的页在置换时有额外的权重，这个权重用时间Ms体现。
    // 默认是60秒。
    // 置换权重 = 上次访问时间戳 + 额外权重，优先从内存中驱逐权重小的页。
    // 例如：一个只读的页，上次访问时间戳是T，一个读写页，上次访问时间是T - 60秒，
    // 这两个页在置换时有同样的权重
    private final long writePageExtraWeightMs;
    private final AtomicLong usedSize = new AtomicLong(0L);
    private final Set<BufferHolder> directBufferHolders = ConcurrentHashMap.newKeySet();
    private final Set<BufferHolder> mMapBufferHolders = ConcurrentHashMap.newKeySet();
    private final Map<Integer, PreLoadCache> bufferCache = new ConcurrentHashMap<>();
    private static PreloadBufferPool instance = null;

    public static PreloadBufferPool getInstance() {
        if(null == instance) {
            instance = new PreloadBufferPool();
            Runtime.getRuntime().addShutdownHook(new Thread(instance::close));
        }
        return instance;
    }

    private PreloadBufferPool() {
        long printMetricInterval = Long.parseLong(System.getProperty(PRINT_METRIC_INTERVAL_MS_KEY,"30000"));
        maxMemorySize = getMaxMemorySize();
        evictMemorySize = Math.round(maxMemorySize * EVICT_RATIO);
        coreMemorySize = Math.round(maxMemorySize * CORE_RATIO);
        writePageExtraWeightMs = Long.parseLong(System.getProperty(WRITE_PAGE_EXTRA_WEIGHT_MS_KEY, String.valueOf(DEFAULT_WRITE_PAGE_EXTRA_WEIGHT_MS)));
        maxPageAge = Integer.parseInt(System.getProperty(MAX_PAGE_AGE, String.valueOf(DEFAULT_MAX_PAGE_AGE)));
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

        logger.info("Max direct memory: {}, core direct memory: {}, evict direct memory: {}.",
                Format.formatSize(maxMemorySize),
                Format.formatSize(coreMemorySize),
                Format.formatSize(evictMemorySize));
    }

    /**
     * 计算可供缓存使用的最大堆外内存。
     *
     * 1. 如果PreloadBufferPool.MaxMemory设置为数值，直接使用设置值。
     * 2. 如果PreloadBufferPool.MaxMemory设置为百分比，比如：90%，最大堆外内存 = 物理内存 * 90% - 最大堆内存（由JVM参数-Xmx配置）
     * 3. 如果PreloadBufferPool.MaxMemory未设置或者设置了非法值，最大堆外内存 = VM.maxDirectMemory() * 90%。
     * 其中VM.maxDirectMemory()取值为JVM参数-XX:MaxDirectMemorySize，如果未设置-XX:MaxDirectMemorySize，取值为JVM参数-Xmx。
     *
     * @return 可使用的最大堆外内存大小。
     */
    private long getMaxMemorySize() {
        String mmsString = System.getProperty(MAX_MEMORY_KEY);
        int pct = Format.getPercentage(mmsString);
        if (pct > 0 && pct < 100) {
            long physicalMemorySize = getPhysicalMemorySize();
            long reservedHeapMemorySize = Runtime.getRuntime().maxMemory();
            if (Long.MAX_VALUE == reservedHeapMemorySize) {
                logger.warn("Runtime.getRuntime().maxMemory() returns unlimited!");
                reservedHeapMemorySize = physicalMemorySize / 2;
            }
            // 如果设置了百分比，最大可使用堆外内存= 物理内存 * 百分比 - 最大堆内存
            long mms = physicalMemorySize * pct / 100 - reservedHeapMemorySize;

            // 最大堆外内存 * CACHE_RATIO，这是使用堆外内存的上限，超过这个上限会导致频繁的FullGC
            long mdm = Math.round(VM.maxDirectMemory() * CACHE_RATIO);

            if (mms > 0 && mms < mdm) {
                return mms;
            } else {
                return mdm;
            }
        }
        return Format.parseSize(System.getProperty(MAX_MEMORY_KEY), Math.round(VM.maxDirectMemory() * CACHE_RATIO));
    }

    private LoopThread buildMetricThread(long printMetricInterval) {
        return LoopThread.builder()
                .name("DirectBufferPrintThread")
                .sleepTime(printMetricInterval, printMetricInterval)
                .doWork(this::printMetric)
                .daemon(true)
                .build();
    }

    private void printMetric() {
        long totalUsed = usedSize.get();
        long plUsed = bufferCache.values().stream().mapToLong(preLoadCache -> {
            long cached = preLoadCache.cache.size();
            long usedPreLoad = preLoadCache.onFlyCounter.get();
            long totalSize = preLoadCache.bufferSize * (cached + usedPreLoad);
            logger.info("PreloadCache usage: cached: {} * {} = {}, used: {} * {} = {}, total: {}",
                    Format.formatSize(preLoadCache.bufferSize), cached, Format.formatSize(preLoadCache.bufferSize * cached),
                    Format.formatSize(preLoadCache.bufferSize), usedPreLoad, Format.formatSize(preLoadCache.bufferSize * usedPreLoad),
                    Format.formatSize(totalSize));
            return totalSize;
        }).sum();
        long mmpUsed = mMapBufferHolders.stream().mapToLong(BufferHolder::capacity).sum();
        long directUsed = directBufferHolders.stream().mapToLong(BufferHolder::capacity).sum();
        logger.info("Direct memory usage: preload/direct/mmp/used/max: {}/{}/{}/{}/{}.",
                Format.formatSize(plUsed),
                Format.formatSize(directUsed),
                Format.formatSize(mmpUsed),
                Format.formatSize(totalUsed),
                Format.formatSize(maxMemorySize));
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
                .condition(this::needEviction)
                .doWork(this::evict)
                .onException(e -> logger.warn("EvictThread exception:", e))
                .daemon(true)
                .build();
    }


    /**
     * 清除文件缓存页。LRU。
     */
    private void evict() {
        // 清理超过maxCount的缓存页
        for (PreLoadCache preLoadCache : bufferCache.values()) {
            if (!needEviction()) {
                break;
            }
            while (preLoadCache.cache.size() > preLoadCache.maxCount && !needEviction()) {
                try {
                    destroyOne(preLoadCache.cache.remove());
                } catch (NoSuchElementException ignored) {}
            }
        }

        List<LruWrapper<BufferHolder>> sortedPage = Stream.concat(directBufferHolders.stream(), mMapBufferHolders.stream())
                .filter(BufferHolder::isFree)
                .map(bufferHolder -> new LruWrapper<>(bufferHolder, bufferHolder.lastAccessTime(), bufferHolder.writable() ? writePageExtraWeightMs : 0L))
                .sorted(Comparator.comparing(LruWrapper::getWeight))
                .collect(Collectors.toList());

        Iterator<LruWrapper<BufferHolder>> sortedPageIterator = sortedPage.iterator();
        while (sortedPageIterator.hasNext()) {
            LruWrapper<BufferHolder> lruWrapper = sortedPageIterator.next();
            if (SystemClock.now() - lruWrapper.getLastAccessTime() >= maxPageAge) {
                lruWrapper.get().evict();
                sortedPageIterator.remove();
            } else {
                break;
            }
        }

        // 清理使用中最旧的页面，直到内存占用率达标
        if (needEviction()) {
            while (needEviction() && !sortedPage.isEmpty()) {
                LruWrapper<BufferHolder> wrapper = sortedPage.remove(0);
                BufferHolder holder = wrapper.get();
                if (holder.lastAccessTime() == wrapper.getLastAccessTime()) {
                    holder.evict();
                }
            }
        }
    }


    private boolean needEviction() {
        return usedSize.get() > evictMemorySize;
    }

    private boolean isOutOfMemory() {
        return usedSize.get() > maxMemorySize;
    }

    private boolean isHungry() {
        return usedSize.get() < coreMemorySize;
    }

    public synchronized boolean addPreLoad(int bufferSize, int coreCount, int maxCount) {
        return bufferCache.putIfAbsent(bufferSize, new PreLoadCache(bufferSize, coreCount, maxCount)) == null;
    }

    private void close() {
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
            if ( preLoadCache.cache.size() < preLoadCache.coreCount ) {
                if (isHungry()) {
                    try {
                        while (preLoadCache.cache.size() < preLoadCache.coreCount && usedSize.get() + preLoadCache.bufferSize < maxMemorySize) {
                            preLoadCache.cache.add(createOne(preLoadCache.bufferSize));
                        }
                    } catch (OutOfMemoryError ignored) {
                        return;
                    }
                } else {
                    List<LruWrapper<BufferHolder>> outdated = directBufferHolders.stream()
                            .filter(b -> b.capacity() == preLoadCache.bufferSize)
                            .filter(BufferHolder::isFree)
                            .map(bufferHolder -> new LruWrapper<>(bufferHolder, bufferHolder.lastAccessTime(), bufferHolder.writable() ? writePageExtraWeightMs : 0L))
                            .sorted(Comparator.comparing(LruWrapper::getWeight))
                            .collect(Collectors.toList());
                    while (preLoadCache.cache.size() < preLoadCache.coreCount && !outdated.isEmpty()) {
                        LruWrapper<BufferHolder> wrapper = outdated.remove(0);
                        BufferHolder holder = wrapper.get();
                        if (holder.lastAccessTime() == wrapper.getLastAccessTime()) {
                            holder.evict();
                        }
                    }
                }
            }
        }
    }

    private ByteBuffer createOne(int size) {
        reserveMemory(size);
        return ByteBuffer.allocateDirect(size);
    }

    private void reserveMemory(int size) {
        usedSize.addAndGet(size);
        try {
            while (isOutOfMemory()) {
                PreLoadCache preLoadCache = bufferCache.values().stream()
                        .filter(p -> p.cache.size() > 0)
                        .findAny().orElse(null);
                if (null != preLoadCache) {
                    destroyOne(preLoadCache.cache.remove());
                } else {
                    break;
                }
            }

            if (isOutOfMemory()) {
                // 如果内存不足，唤醒清理线程立即执行清理
                evictThread.wakeup();
                // 等待5x10ms，如果还不足抛出异常
                for (int i = 0; i < 5 && isOutOfMemory(); i++) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        logger.warn("Interrupted: ", e);
                    }
                }
                if (isOutOfMemory()) {
                    throw new OutOfMemoryError();
                }
            }
        } catch (Throwable t) {
            usedSize.getAndAdd( -1 * size);
        }
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

    public void allocateMMap(BufferHolder bufferHolder) {
        reserveMemory(bufferHolder.capacity());
        mMapBufferHolders.add(bufferHolder);
    }

    public ByteBuffer allocateDirect(BufferHolder bufferHolder) {
        ByteBuffer buffer = allocateDirect(bufferHolder.capacity());
        directBufferHolders.add(bufferHolder);
        return buffer;
    }

    private ByteBuffer allocateDirect(int bufferSize) {
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
            logger.debug("OOM: {}/{}.", Format.formatSize(usedSize.get()), Format.formatSize(maxMemorySize));
            throw outOfMemoryError;
        }
    }

    public void releaseDirect(ByteBuffer byteBuffer, BufferHolder bufferHolder) {
        directBufferHolders.remove(bufferHolder);
        int size = byteBuffer.capacity();
        PreLoadCache preLoadCache = bufferCache.get(size);
        if (null != preLoadCache) {
            if (needEviction() && preLoadCache.cache.size() >= preLoadCache.maxCount) {
                destroyOne(byteBuffer);
            } else {
                byteBuffer.clear();
                preLoadCache.cache.add(byteBuffer);
            }
            preLoadCache.onFlyCounter.getAndDecrement();
        } else {
            destroyOne(byteBuffer);
        }
    }

    public void releaseMMap(BufferHolder bufferHolder) {
        mMapBufferHolders.remove(bufferHolder);
        usedSize.getAndAdd(-1 * bufferHolder.capacity());

    }

    private static long getPhysicalMemorySize() {
        com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean)
                java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        return os.getTotalPhysicalMemorySize();
    }
    /**
     * buffer监控
     */
    public BufferPoolMonitorInfo monitorInfo() {
        BufferPoolMonitorInfo bufferPoolMonitorInfo = new BufferPoolMonitorInfo();

        List<BufferPoolMonitorInfo.PLMonitorInfo> plMonitorInfos = new ArrayList<>();
        long totalUsed = usedSize.get();
        long plUsed = bufferCache.values().stream().mapToLong(preLoadCache -> {
            long cached = preLoadCache.cache.size();
            long usedPreLoad = preLoadCache.onFlyCounter.get();
            long totalSize = preLoadCache.bufferSize * (cached + usedPreLoad);
            BufferPoolMonitorInfo.PLMonitorInfo plMonitorInfo = new BufferPoolMonitorInfo.PLMonitorInfo();
            plMonitorInfo.setBufferSize(Format.formatSize(preLoadCache.bufferSize));
            plMonitorInfo.setCached(Format.formatSize(preLoadCache.bufferSize * cached));
            plMonitorInfo.setUsedPreLoad(Format.formatSize(preLoadCache.bufferSize * usedPreLoad));
            plMonitorInfo.setTotalSize(Format.formatSize(totalSize));
            plMonitorInfos.add(plMonitorInfo);
            return totalSize;
        }).sum();
        long mmpUsed = mMapBufferHolders.stream().mapToLong(BufferHolder::capacity).sum();
        long directUsed = directBufferHolders.stream().mapToLong(BufferHolder::capacity).sum();

        bufferPoolMonitorInfo.setPlMonitorInfos(plMonitorInfos);
        bufferPoolMonitorInfo.setPlUsed(Format.formatSize(plUsed));
        bufferPoolMonitorInfo.setUsed(Format.formatSize(totalUsed));
        bufferPoolMonitorInfo.setMaxMemorySize(Format.formatSize(maxMemorySize));
        bufferPoolMonitorInfo.setMmpUsed(Format.formatSize(mmpUsed));
        bufferPoolMonitorInfo.setDirectUsed(Format.formatSize(directUsed));
        return bufferPoolMonitorInfo;
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
        private final long extraWeight;
        private final V t;

        LruWrapper(V t, long lastAccessTime, long extraWeight) {
            this.lastAccessTime = lastAccessTime;
            this.t = t;
            this.extraWeight = extraWeight;
        }

        private long getLastAccessTime() {
            return lastAccessTime;
        }

        private V get() {
            return t;
        }

        private long getWeight() {
            return lastAccessTime + extraWeight;
        }
    }
}
