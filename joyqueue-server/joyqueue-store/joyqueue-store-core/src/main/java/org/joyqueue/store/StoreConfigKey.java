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
package org.joyqueue.store;

import org.joyqueue.store.file.PositioningStore;
import org.joyqueue.toolkit.config.PropertyDef;

import static org.joyqueue.store.PartitionGroupStoreManager.Config.DEFAULT_FLUSH_INTERVAL_MS;
import static org.joyqueue.store.PartitionGroupStoreManager.Config.DEFAULT_MAX_DIRTY_SIZE;
import static org.joyqueue.store.PartitionGroupStoreManager.Config.DEFAULT_MAX_MESSAGE_LENGTH;
import static org.joyqueue.store.PartitionGroupStoreManager.Config.DEFAULT_WRITE_REQUEST_CACHE_SIZE;
import static org.joyqueue.store.PartitionGroupStoreManager.Config.DEFAULT_WRITE_TIMEOUT_MS;
import static org.joyqueue.store.file.PositioningStore.Config.DEFAULT_FILE_HEADER_SIZE;

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
    MESSAGE_FILE_SIZE("store.message.file.size", StoreConfig.DEFAULT_MESSAGE_FILE_SIZE, Type.INT),
    /**
     * 索引文件大小 1M
     */
    INDEX_FILE_SIZE("store.index.file.size", StoreConfig.DEFAULT_INDEX_FILE_SIZE, Type.INT),

    /**
     * 预读取消息文件
     */
    MESSAGE_FILE_LOAD_ON_READ("store.message.file.load", StoreConfig.DEFAULT_MESSAGE_FILE_LOAD_ON_READ, Type.BOOLEAN),
    /**
     * 预读取索引文件
     */
    INDEX_FILE_LOAD_ON_READ("store.index.file.load", StoreConfig.DEFAULT_INDEX_FILE_LOAD_ON_READ, Type.BOOLEAN),

    /**
     * 预加载DirectBuffer的核心数量
     */
    PRELOAD_BUFFER_CORE_COUNT("store.preload.buffer.core.count", StoreConfig.DEFAULT_PRE_LOAD_BUFFER_CORE_COUNT, Type.INT),
    /**
     * 预加载DirectBuffer的最大数量
     */
    PRELOAD_BUFFER_MAX_COUNT("store.preload.buffer.max.count", StoreConfig.DEFAULT_PRE_LOAD_BUFFER_MAX_COUNT, Type.INT),
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
     * 是否强制刷盘
     */
    FLUSH_FORCE("store.flush.force", StoreConfig.DEFAULT_FLUSH_FORCE, Type.BOOLEAN),
    /**
     * 文件头长度
     */
    FILE_HEADER_SIZE("store.file.header.size", DEFAULT_FILE_HEADER_SIZE, Type.INT),

    MAX_DIRTY_SIZE("store.max.dirty.size", DEFAULT_MAX_DIRTY_SIZE, Type.LONG),

    DISK_FULL_RATIO("store.disk.full.ratio", PositioningStore.Config.DEFAULT_DISK_FULL_RATIO, Type.INT),

    PRINT_METRIC_INTERVAL_MS("print.metric.interval", StoreConfig.DEFAULT_PRINT_METRIC_INTERVAL_MS, Type.LONG);


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

