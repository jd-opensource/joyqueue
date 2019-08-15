package io.chubao.joyqueue.broker.store;

import io.chubao.joyqueue.broker.config.BrokerStoreConfig;
import io.chubao.joyqueue.store.PartitionGroupStore;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.time.SystemClock;

import java.io.IOException;
import java.util.Map;

/**
 * @author majun8
 */
public class IntervalTimeStoreCleaningStrategy implements StoreCleaningStrategy {
    private long maxIntervalTime;
    private boolean doNotDeleteConsumed = true;

    public IntervalTimeStoreCleaningStrategy() {

    }

    @Override
    public long deleteIfNeeded(PartitionGroupStore partitionGroupStore, Map<Short, Long> partitionAckMap) throws IOException {
        long currentTimestamp = SystemClock.now();
        long targetDeleteTimeline = currentTimestamp - maxIntervalTime;

        long totalDeletedSize = 0L;  // 总共删除长度
        long deletedSize = 0L;

        if (partitionGroupStore != null) {
            do {
                deletedSize = partitionGroupStore.deleteMinStoreMessages(targetDeleteTimeline, partitionAckMap, doNotDeleteConsumed);
                totalDeletedSize += deletedSize;
            } while (deletedSize > 0L);
        }

        return totalDeletedSize;
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        BrokerStoreConfig brokerStoreConfig = new BrokerStoreConfig(supplier);
        this.maxIntervalTime = brokerStoreConfig.getMaxStoreTime();
        this.doNotDeleteConsumed = brokerStoreConfig.getDoNotDeleteConsumed();
    }
}
