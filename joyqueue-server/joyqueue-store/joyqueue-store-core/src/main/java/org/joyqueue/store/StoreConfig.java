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
import org.joyqueue.toolkit.config.Property;
import org.joyqueue.toolkit.config.PropertySupplier;

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
    public static final int DEFAULT_PRE_LOAD_BUFFER_CORE_COUNT = 3;
    public static final int DEFAULT_PRE_LOAD_BUFFER_MAX_COUNT = 10;
    public static final long DEFAULT_PRINT_METRIC_INTERVAL_MS = 0;
    public static final boolean DEFAULT_MESSAGE_FILE_LOAD_ON_READ = false;
    public static final boolean DEFAULT_INDEX_FILE_LOAD_ON_READ = true;
    public static final boolean DEFAULT_FLUSH_FORCE = true;

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
     * 预加载DirectBuffer的核心数量
     */
    private int preLoadBufferCoreCount = DEFAULT_PRE_LOAD_BUFFER_CORE_COUNT;
    /**
     * 预加载DirectBuffer的最大数量
     */
    private int preLoadBufferMaxCount = DEFAULT_PRE_LOAD_BUFFER_MAX_COUNT;

    private long printMetricIntervalMs = DEFAULT_PRINT_METRIC_INTERVAL_MS;

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
     * 文件头长度
     */
    private int fileHeaderSize = PositioningStore.Config.DEFAULT_FILE_HEADER_SIZE;

    private long maxDirtySize = PartitionGroupStoreManager.Config.DEFAULT_MAX_DIRTY_SIZE;

    private int diskFullRatio = PositioningStore.Config.DEFAULT_DISK_FULL_RATIO;


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

    public boolean isFlushForce() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.FLUSH_FORCE, DEFAULT_FLUSH_FORCE);
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


    public long getWriteTimeoutMs() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.WRITE_TIMEOUT, this.writeTimeoutMs);
    }

    public void setWriteTimeoutMs(long writeTimeoutMs) {
        this.writeTimeoutMs = writeTimeoutMs;
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


    public long getPrintMetricIntervalMs() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.PRINT_METRIC_INTERVAL_MS, printMetricIntervalMs);
    }

    public void setPrintMetricIntervalMs(long printMetricIntervalMs) {
        this.printMetricIntervalMs = printMetricIntervalMs;
    }

    public int getDiskFullRatio() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.DISK_FULL_RATIO, this.diskFullRatio);
    }

    public void setDiskFullRatio(int diskFullRatio) {
        this.diskFullRatio = diskFullRatio;
    }

    public boolean isMessageFileLoadOnRead() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.MESSAGE_FILE_LOAD_ON_READ, DEFAULT_MESSAGE_FILE_LOAD_ON_READ);
    }

    public boolean isIndexFileLoadOnRead() {
        return PropertySupplier.getValue(propertySupplier, StoreConfigKey.INDEX_FILE_LOAD_ON_READ, DEFAULT_INDEX_FILE_LOAD_ON_READ);
    }

}

