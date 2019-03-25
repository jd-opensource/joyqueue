package com.jd.journalq.broker.archive;

import com.jd.journalq.toolkit.config.Property;
import com.jd.journalq.toolkit.config.PropertySupplier;

/**
 * 归档配置
 * <p>
 * Created by chengzhiliang on 2018/12/6.
 */
public class ArchiveConfig {
    private static final String ARCHIVE_PATH ="/archive";
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

    public int getWriteBatchNum() {
        return PropertySupplier.getValue(propertySupplier, ArchiveConfigKey.WRITE_BATCH_NUM);
    }

    public int getReadBatchNum() {
        return PropertySupplier.getValue(propertySupplier, ArchiveConfigKey.READ_BATCH_NUM);
    }

    public int getLogQueueSize() {
        return PropertySupplier.getValue(propertySupplier, ArchiveConfigKey.LOG_QUEUE_SIZE);
    }

    public int getWriteThreadNum() {
        return PropertySupplier.getValue(propertySupplier, ArchiveConfigKey.WRITE_THREAD_NUM);
    }
}
