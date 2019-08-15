package io.chubao.joyqueue.broker.store;

import io.chubao.joyqueue.store.PartitionGroupStore;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;

import java.io.IOException;
import java.util.Map;

/**
 * @author majun8
 */
public interface StoreCleaningStrategy extends PropertySupplierAware {

    long deleteIfNeeded(PartitionGroupStore partitionGroupStore, Map<Short, Long> partitionAckMap) throws IOException;
}
