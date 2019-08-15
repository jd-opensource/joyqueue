package io.chubao.joyqueue.broker.store;

import io.chubao.joyqueue.broker.config.BrokerStoreConfig;
import io.chubao.joyqueue.store.PartitionGroupStore;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;

import java.io.IOException;
import java.util.Map;

/**
 * @author majun8
 */
public class FixedSizeStoreCleaningStrategy implements StoreCleaningStrategy {
    private long maxStorageSize;
    private boolean doNotDeleteConsumed = true;

    public FixedSizeStoreCleaningStrategy() {

    }

    @Override
    public long deleteIfNeeded(PartitionGroupStore partitionGroupStore, Map<Short, Long> partitionAckMap) throws IOException {
        long totalDeletedSize = 0L;  // 总共删除长度

        if (partitionGroupStore != null) {
            long currentTotalStorageSize = partitionGroupStore.getTotalPhysicalStorageSize();
            if( maxStorageSize < currentTotalStorageSize ) {
                long targetDeleteSize = currentTotalStorageSize - maxStorageSize;  // 目标删除长度

                long lastDeletedSize;  // 上一次删除长度
                do {
                    lastDeletedSize = partitionGroupStore.deleteMinStoreMessages(0, partitionAckMap, doNotDeleteConsumed);
                } while (lastDeletedSize > 0L && (totalDeletedSize += lastDeletedSize) < targetDeleteSize);
            }
        }

        return totalDeletedSize;
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        BrokerStoreConfig brokerStoreConfig = new BrokerStoreConfig(supplier);
        this.maxStorageSize = brokerStoreConfig.getMaxStoreSize();
        this.doNotDeleteConsumed = brokerStoreConfig.getDoNotDeleteConsumed();
    }
}
