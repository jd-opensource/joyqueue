package com.jd.journalq.store;

import com.jd.journalq.toolkit.config.PropertyDef;

import static com.jd.journalq.store.PartitionGroupStoreManager.Config.DEFAULT_EVICT_INTERVAL_MS;
import static com.jd.journalq.store.PartitionGroupStoreManager.Config.*;
import static com.jd.journalq.store.file.PositioningStore.Config.*;

/**
 * 存储配置
 * 总磁盘大小 = PartitionGroup 数量 * partitionGroupMaxStoreSize
 *
 * @author liyue25
 * Date: 2018/9/10
 */
public enum StoreConfigKey implements PropertyDef {
    /**
     * 消息文件大小 128M
     */
    MESSAGE_FILE_SIZE("store.message.file.size", 128 * 1024 * 1024, Type.INT),
    /**
     * 索引文件大小 1M
     */
    INDEX_FILE_SIZE("store.index.file.size", 512 * 1024, Type.INT),
    /**
     * 虚拟线程执行器的线程数量
     */
    THREAD_COUNT("store.thread.count", 4, Type.INT),
    /**
     * 预加载DirectBuffer的核心数量
     */
    PRELOAD_BUFFER_CORE_COUNT("store.preload.buffer.core.count", 0, Type.INT),
    /**
     * 预加载DirectBuffer的最大数量
     */
    PRELOAD_BUFFER_MAX_COUNT("store.preload.buffer.max.count", 10, Type.INT),
    /**
     * 最大消息长度
     */
    MAX_MESSAGE_LENGTH("store.max.message.length", DEFAULT_MAX_MESSAGE_LENGTH, Type.INT),
    /**
     * 写入请求缓存的大小
     */
    WRITE_REQUEST_CACHE_SIZE("store.write.request.cache.size", DEFAULT_WRITE_REQUEST_CACHE_SIZE, Type.INT),
    /**
     * 写入超时时间
     */
    WRITE_TIMEOUT("store.write.timeout", DEFAULT_WRITE_TIMEOUT_MS, Type.LONG),
    /**
     * 异步刷盘的时间间隔(ms)
     */
    FLUSH_INTERVAL_MS("store.flush.interval", DEFAULT_FLUSH_INTERVAL_MS, Type.LONG),
    /**
     * 文件头长度
     */
    FILE_HEADER_SIZE("store.file.header.size", DEFAULT_FILE_HEADER_SIZE, Type.INT),
    /**
     * 最多缓存的页面数量
     */
    CACHED_PAGE_COUNT("store.cached.page.count", DEFAULT_CACHED_PAGE_COUNT, Type.INT),
    /**
     * 缓存最长存活时间
     */
    CACHE_LIFE_TIME_MS("store.cache.life.time", DEFAULT_CACHE_LIFETIME_MS, Type.LONG),
    /**
     * 存储上限，超过上限后，最旧的文件将被删除
     */
    MAX_STORE_SIZE("store.max.store.size", DEFAULT_MAX_STORE_SIZE, Type.LONG),
    MAX_STORE_TIME("store.max.store.time", DEFAULT_MAX_STORE_TIME, Type.LONG),
    MAX_DIRTY_SIZE("store.max.dirty.size", DEFAULT_MAX_DIRTY_SIZE, Type.LONG),

    EVECT_INTERVAL_MS("store.evict.interval",DEFAULT_EVICT_INTERVAL_MS,Type.LONG);


    private String name;
    private Object value;
    private Type type;

    StoreConfigKey(String name, Object value, Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return type;
    }
}
