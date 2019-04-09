package com.jd.journalq.broker.store;

import com.jd.journalq.broker.config.BrokerStoreConfig;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.store.PartitionGroupStore;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.toolkit.config.PropertySupplier;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author majun8
 */
public class FixedSizeStoreCleaningStrategy implements StoreCleaningStrategy {
    private long maxStorageSize;

    public FixedSizeStoreCleaningStrategy() {

    }

    @Override
    public long deleteIfNeeded(PartitionGroupStore partitionGroupStore, long minIndexedPosition) throws IOException {
        long totalDeletedSize = 0L;  // 总共删除长度

        if (partitionGroupStore != null) {
            long currentTotalStorageSize = partitionGroupStore.getTotalPhysicalStorageSize();
            if( maxStorageSize < currentTotalStorageSize ) {
                long targetDeleteSize = currentTotalStorageSize - maxStorageSize;  // 目标删除长度

                long lastDeletedSize;  // 上一次删除长度
                do {
                    lastDeletedSize = partitionGroupStore.deleteMinStoreMessages(minIndexedPosition);
                } while (lastDeletedSize > 0L && (totalDeletedSize += lastDeletedSize) < targetDeleteSize);
            }
        }

        return totalDeletedSize;
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.maxStorageSize = new BrokerStoreConfig(supplier).getMaxStoreSize();
    }
}
