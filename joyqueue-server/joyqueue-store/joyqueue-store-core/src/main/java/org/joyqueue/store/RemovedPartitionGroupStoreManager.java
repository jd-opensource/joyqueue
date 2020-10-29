package org.joyqueue.store;

import org.joyqueue.store.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * RemovedPartitionGroupStoreManager
 * author: gaohaoxiang
 * date: 2020/10/28
 */
public class RemovedPartitionGroupStoreManager implements RemovedPartitionGroupStore {

    protected static final Logger logger = LoggerFactory.getLogger(RemovedPartitionGroupStoreManager.class);

    private String topic;
    private int partitionGroup;
    private File base;
    private List<File> storeFiles;
    private Map<Short, List<File>> indexStoreFiles;

    public RemovedPartitionGroupStoreManager(String topic, int partitionGroup, File base, List<File> storeFiles,
                                             Map<Short, List<File>> indexStoreFiles) {
        this.topic = topic;
        this.partitionGroup = partitionGroup;
        this.base = base;
        this.storeFiles = storeFiles;
        this.indexStoreFiles = indexStoreFiles;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public int getPartitionGroup() {
        return partitionGroup;
    }

    @Override
    public boolean physicalDeleteLeftFile() {
        boolean deleteStoreFile = false;
        boolean deleteIndexStoreFile = false;

        File storeFile = (storeFiles.isEmpty() ? null : storeFiles.remove(0));
        if (storeFile != null) {
            deleteStoreFile = physicalDeleteFile(base, storeFile.getName());
        }

        for (Map.Entry<Short, List<File>> entry : indexStoreFiles.entrySet()) {
            List<File> indexStoreFiles = entry.getValue();
            File indexStoreFile = (indexStoreFiles.isEmpty() ? null : indexStoreFiles.remove(0));
            if (indexStoreFile != null) {
                physicalDeleteFile(new File(base, "index" + File.separator + entry.getKey()), indexStoreFile.getName());
                deleteIndexStoreFile = true;
            }
        }

        return deleteStoreFile || deleteIndexStoreFile;
    }

    @Override
    public boolean physicalDelete() {
        return physicalDeleteFile(base);
    }

    protected boolean physicalDeleteFile(File base, String file) {
        return physicalDeleteFile(new File(base, file));
    }

    protected boolean physicalDeleteFile(File file) {
        if (logger.isDebugEnabled()) {
            logger.debug("Store file deleted, file: {}", base, file);
        }
        if (!file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            return FileUtils.deleteFolder(file);
        } else {
            return file.delete();
        }
    }
}