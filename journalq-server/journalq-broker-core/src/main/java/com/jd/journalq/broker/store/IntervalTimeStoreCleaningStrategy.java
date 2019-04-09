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
public class IntervalTimeStoreCleaningStrategy implements StoreCleaningStrategy {
    private long maxIntervalTime;

    public IntervalTimeStoreCleaningStrategy() {

    }

    @Override
    public long deleteIfNeeded(PartitionGroupStore partitionGroupStore, long minIndexedPosition) throws IOException {
        long currentTimestamp = System.currentTimeMillis();
        long targetDeleteTimeline = currentTimestamp - maxIntervalTime;

        long totalDeletedSize = 0L;  // 总共删除长度
        long deletedSize = 0L;

        if (partitionGroupStore != null) {
            do {
                deletedSize = partitionGroupStore.deleteEarlyMinStoreMessages(targetDeleteTimeline, minIndexedPosition);
                totalDeletedSize += deletedSize;
            } while (deletedSize > 0L);
        }

        return totalDeletedSize;
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.maxIntervalTime = new BrokerStoreConfig(supplier).getMaxStoreTime();
    }
}
