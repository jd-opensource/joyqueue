package com.jd.journalq.store;

import com.jd.journalq.store.file.PositioningStore;
import com.jd.journalq.toolkit.config.Property;
import com.jd.journalq.toolkit.config.PropertySupplier;

/**
 * 存储配置
 * 总磁盘大小 = PartitionGroup 数量 * partitionGroupMaxStoreSize
 *
 * @author liyue25
 * Date: 2018/9/10
 */
public class StoreConfig {

    public static final int DEFAULT_MESSAGE_FILE_SIZE = 128 * 1024 * 1024;
    public static final int DEFAULT_INDEX_FILE_SIZE = 512 * 1024;
    public static final int DEFAULT_THREAD_COUNT = 4;
    public static final int DEFAULT_PRE_LOAD_BUFFER_CORE_COUNT = 0;
    public static final int DEFAULT_PRE_LOAD_BUFFER_MAX_COUNT = 10;
    public static final String STORE_PATH = "/store";
    /**
     * 存储路径
     */
    private String storePath;
    /**
     * 消息文件大小 128M
     */
    private int messageFileSize = DEFAULT_MESSAGE_FILE_SIZE;
    /**
     * 索引文件大小 1M
     */
    private int indexFileSize = DEFAULT_INDEX_FILE_SIZE;

    /**
     * 虚拟线程执行器的线程数量
     */
    private int threadCount = DEFAULT_THREAD_COUNT;

    /**
     * 预加载DirectBuffer的核心数量
     */
    private int preLoadBufferCoreCount = DEFAULT_PRE_LOAD_BUFFER_CORE_COUNT;
    /**
     * 预加载DirectBuffer的最大数量
     */
    private int preLoadBufferMaxCount = DEFAULT_PRE_LOAD_BUFFER_MAX_COUNT;

    /**
     * 最大消息长度
     */
    private int maxMessageLength = PartitionGroupStoreManager.Config.DEFAULT_MAX_MESSAGE_LENGTH;
    /**
     * 写入请求缓存的大小
     */
    private int writeRequestCacheSize = PartitionGroupStoreManager.Config.DEFAULT_WRITE_REQUEST_CACHE_SIZE;

    /**
     * 写入超时时间
     */
    private long writeTimeoutMs = PartitionGroupStoreManager.Config.DEFAULT_WRITE_TIMEOUT_MS;

    /**
     * 异步刷盘的时间间隔(ms)
     */
    private long flushIntervalMs = PartitionGroupStoreManager.Config.DEFAULT_FLUSH_INTERVAL_MS;
    /**
     * 异步刷盘的时间间隔(ms)
     */
    private long evictIntervalMs = PartitionGroupStoreManager.Config.DEFAULT_EVICT_INTERVAL_MS;
    /**
     * 文件头长度
     */
    private int fileHeaderSize = PositioningStore.Config.DEFAULT_FILE_HEADER_SIZE;
    /**
     * 最多缓存的页面数量
     */
    private int cachedPageCount = PositioningStore.Config.DEFAULT_CACHED_PAGE_COUNT;

    /**
     * 缓存最长存活时间
     */
    private long cacheLifeTimeMs = PositioningStore.Config.DEFAULT_CACHE_LIFETIME_MS;

    /**
     * 存储上限，超过上限后，最旧的文件将被删除
     */
    private long maxStoreSize = PartitionGroupStoreManager.Config.DEFAULT_MAX_STORE_SIZE;  // 10gb

    private long maxStoreTime = PartitionGroupStoreManager.Config.DEFAULT_MAX_STORE_TIME;  // 7days

    private long maxDirtySize = PartitionGroupStoreManager.Config.DEFAULT_MAX_DIRTY_SIZE;


    private PropertySupplier propertySupplier;

    public StoreConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public String getPath() {
        if (storePath == null || storePath.isEmpty()) {
            synchronized (this) {
                if (storePath == null) {
                    String prefix = "";
                    if (propertySupplier != null) {
                        Property property = propertySupplier.getProperty(Property.APPLICATION_DATA_PATH);
                        prefix = property == null ? prefix : property.getString();
                    }
                    storePath = prefix + STORE_PATH;
                }

            }
        }
        return storePath;
    }

    public void setPath(String path) {
        if (path != null && !path.isEmpty()) {
            this.storePath = path;
        }
    }

    public int getMessageFileSize() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.MESSAGE_FILE_SIZE, this.messageFileSize);
    }

    public void setMessageFileSize(int messageFileSize) {
        this.messageFileSize = messageFileSize;
    }

    public int getIndexFileSize() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.INDEX_FILE_SIZE, this.indexFileSize);
    }

    public void setIndexFileSize(int indexFileSize) {
        this.indexFileSize = indexFileSize;
    }

    public int getMaxMessageLength() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.MAX_MESSAGE_LENGTH, this.maxMessageLength);
    }

    public void setMaxMessageLength(int maxMessageLength) {
        this.maxMessageLength = maxMessageLength;
    }

    public int getWriteRequestCacheSize() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.WRITE_REQUEST_CACHE_SIZE, this.writeRequestCacheSize);
    }

    public void setWriteRequestCacheSize(int writeRequestCacheSize) {
        this.writeRequestCacheSize = writeRequestCacheSize;
    }

    public long getFlushIntervalMs() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.FLUSH_INTERVAL_MS, this.flushIntervalMs);
    }

    public void setFlushIntervalMs(long flushIntervalMs) {
        this.flushIntervalMs = flushIntervalMs;
    }

    public int getFileHeaderSize() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.FILE_HEADER_SIZE, this.fileHeaderSize);
    }

    public void setFileHeaderSize(int fileHeaderSize) {
        this.fileHeaderSize = fileHeaderSize;
    }

    public int getCachedPageCount() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.CACHED_PAGE_COUNT, this.cachedPageCount);
    }

    public void setCachedPageCount(int cachedPageCount) {
        this.cachedPageCount = cachedPageCount;
    }

    public long getWriteTimeoutMs() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.WRITE_TIMEOUT, this.writeTimeoutMs);
    }

    public void setWriteTimeoutMs(long writeTimeoutMs) {
        this.writeTimeoutMs = writeTimeoutMs;
    }

    public long getCacheLifeTimeMs() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.CACHE_LIFE_TIME_MS, this.cacheLifeTimeMs);
    }

    public void setCacheLifeTimeMs(long cacheLifeTimeMs) {
        this.cacheLifeTimeMs = cacheLifeTimeMs;
    }

    public long getMaxStoreSize() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.MAX_STORE_SIZE, this.maxStoreSize);
    }

    public void setMaxStoreSize(long maxStoreSize) {
        this.maxStoreSize = maxStoreSize;
    }

    public long getMaxStoreTime() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.MAX_STORE_TIME, this.maxStoreTime);
    }

    public void setMaxStoreTime(long maxStoreTime) {
        this.maxStoreTime = maxStoreTime;
    }

    public int getThreadCount() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.THREAD_COUNT, this.threadCount);
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getPreLoadBufferCoreCount() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.PRELOAD_BUFFER_CORE_COUNT, this.preLoadBufferCoreCount);
    }

    public void setPreLoadBufferCoreCount(int preLoadBufferCoreCount) {
        this.preLoadBufferCoreCount = preLoadBufferCoreCount;
    }

    public int getPreLoadBufferMaxCount() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.PRELOAD_BUFFER_MAX_COUNT, this.preLoadBufferMaxCount);
    }

    public void setPreLoadBufferMaxCount(int preLoadBufferMaxCount) {
        this.preLoadBufferMaxCount = preLoadBufferMaxCount;
    }

    public long getMaxDirtySize() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.MAX_DIRTY_SIZE, this.maxDirtySize);
    }

    public void setMaxDirtySize(long maxDirtySize) {
        this.maxDirtySize = maxDirtySize;
    }

    public long getEvictIntervalMs() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.EVECT_INTERVAL_MS,evictIntervalMs);
    }
}
