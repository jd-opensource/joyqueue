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
package org.joyqueue.broker.archive;

import org.apache.commons.lang3.StringUtils;
import org.joyqueue.config.BrokerConfigKey;
import org.joyqueue.toolkit.config.Property;
import org.joyqueue.toolkit.config.PropertySupplier;

/**
 * 归档配置
 * <p>
 * Created by chengzhiliang on 2018/12/6.
 */
public class ArchiveConfig {
    public static final String LOG_DETAIL_PRODUCE_PREFIX = "produce.";
    public static final String LOG_DETAIL_CONSUME_PREFIX = "consume.";

    private static final String ARCHIVE_PATH ="/archive/";
    private PropertySupplier propertySupplier;
    private String archivePath;

    public ArchiveConfig() {
    }

    public ArchiveConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public String getArchivePath() {
        if (archivePath == null || archivePath.isEmpty()) {
            synchronized (this) {
                if (archivePath == null) {
                    String prefix = "";
                    if (propertySupplier != null) {
                        Property property = propertySupplier.getProperty(Property.APPLICATION_DATA_PATH);
                        prefix = property == null ? prefix : property.getString();
                    }
                    archivePath = prefix + ARCHIVE_PATH;
                }

            }
        }

        return archivePath;
    }

    public void setPath(String path) {
        if (path != null && !path.isEmpty()) {
            archivePath = path;
        }
    }

    public boolean getLogDetail(String archiveType, String brokerId) {
        return (boolean) PropertySupplier.getValue(propertySupplier,
                ArchiveConfigKey.ARCHIVE_TRACE_LOG.getName() + archiveType + brokerId,
                ArchiveConfigKey.ARCHIVE_TRACE_LOG.getType(),
                ArchiveConfigKey.ARCHIVE_TRACE_LOG.getValue());
    }

    public int getConsumeBatchNum() {
        return PropertySupplier.getValue(propertySupplier, ArchiveConfigKey.CONSUME_BATCH_NUM);
    }
    public int getConsumeWriteDelay() {
        return PropertySupplier.getValue(propertySupplier, ArchiveConfigKey.CONSUME_WRITE_DELAY);
    }

    public int getProduceBatchNum() {
        return PropertySupplier.getValue(propertySupplier, ArchiveConfigKey.PRODUCE_BATCH_NUM);
    }

    public int getLogQueueSize() {
        return PropertySupplier.getValue(propertySupplier, ArchiveConfigKey.LOG_QUEUE_SIZE);
    }

    public int getWriteThreadNum() {
        return PropertySupplier.getValue(propertySupplier, ArchiveConfigKey.WRITE_THREAD_NUM);
    }

    public boolean isStartArchive() {
        return PropertySupplier.getValue(propertySupplier, ArchiveConfigKey.ARCHIVE_SWITCH);
    }

    public int getThreadPoolQueueSize() {
        return PropertySupplier.getValue(propertySupplier, ArchiveConfigKey.ARCHIVE_THREAD_POOL_QUEUE_SIZE);
    }

    public String getNamespace() {
        return PropertySupplier.getValue(propertySupplier, ArchiveConfigKey.ARCHIVE_STORE_NAMESPACE);
    }

    public int getStoreFialedRetryCount() {
        return PropertySupplier.getValue(propertySupplier, ArchiveConfigKey.ARCHIVE_STORE_RETRY_COUNT);
    }

    public String getTracerType() {
        return PropertySupplier.getValue(propertySupplier, BrokerConfigKey.TRACER_TYPE);
    }

    public boolean isReamingEnable() {
        return PropertySupplier.getValue(propertySupplier, ArchiveConfigKey.ARCHIVE_REAMING_ENABLE);
    }

    public boolean isBacklogEnable() {
        return PropertySupplier.getValue(propertySupplier, ArchiveConfigKey.ARCHIVE_BACKLOG_ENABLE);
    }

    public int getLogRetainDuration() {
        return PropertySupplier.getValue(propertySupplier, ArchiveConfigKey.ARCHIVE_LOG_RETAIN_DURATION);
    }

    public int getProduceArchiveRate() {
        return propertySupplier.getValue(ArchiveConfigKey.ARCHIVE_PRODUCE_RATE);
    }

    public int getConsumeArchiveRate() {
        return propertySupplier.getValue(ArchiveConfigKey.ARCHIVE_CONSUME_RATE);
    }

    public int getProduceArchiveRate(String topic, String app) {
        if (StringUtils.isEmpty(app)) {
            return PropertySupplier.getValue(propertySupplier,ArchiveConfigKey.ARCHIVE_PRODUCE_RATE_PREFIX.getName() + String.format("%s",topic),
                    ArchiveConfigKey.ARCHIVE_PRODUCE_RATE_PREFIX.getType(), ArchiveConfigKey.ARCHIVE_PRODUCE_RATE_PREFIX.getValue()
            );
        } else {
            return PropertySupplier.getValue(propertySupplier,ArchiveConfigKey.ARCHIVE_PRODUCE_RATE_PREFIX.getName() + String.format("%s.%s",topic,app),
                    ArchiveConfigKey.ARCHIVE_PRODUCE_RATE_PREFIX.getType(), ArchiveConfigKey.ARCHIVE_PRODUCE_RATE_PREFIX.getValue()
            );
        }
    }

    public int getConsumeArchiveRate(String topic, String app) {
        if (StringUtils.isEmpty(app)) {
            return PropertySupplier.getValue(propertySupplier,ArchiveConfigKey.ARCHIVE_CONSUME_RATE_PREFIX.getName() + String.format("%s",topic),
                    ArchiveConfigKey.ARCHIVE_CONSUME_RATE_PREFIX.getType(), ArchiveConfigKey.ARCHIVE_CONSUME_RATE_PREFIX.getValue()
            );
        } else {
            return PropertySupplier.getValue(propertySupplier,ArchiveConfigKey.ARCHIVE_CONSUME_RATE_PREFIX.getName() + String.format("%s.%s",topic,app),
                    ArchiveConfigKey.ARCHIVE_CONSUME_RATE_PREFIX.getType(), ArchiveConfigKey.ARCHIVE_CONSUME_RATE_PREFIX.getValue()
            );
        }
    }
}
