package com.jd.journalq.broker.store;

import com.jd.journalq.domain.TopicName;
import com.jd.journalq.store.PartitionGroupStore;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.toolkit.config.PropertySupplierAware;

import java.io.IOException;

/**
 * @author majun8
 */
public interface StoreCleaningStrategy extends PropertySupplierAware {

    long deleteIfNeeded(PartitionGroupStore partitionGroupStore, long minIndexedPosition) throws IOException;
}
