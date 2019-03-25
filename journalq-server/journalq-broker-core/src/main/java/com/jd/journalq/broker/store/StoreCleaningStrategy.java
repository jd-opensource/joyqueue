package com.jd.journalq.broker.store;

import com.jd.journalq.common.domain.TopicName;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.toolkit.config.PropertySupplierAware;

import java.io.IOException;

/**
 * @author majun8
 */
public interface StoreCleaningStrategy extends PropertySupplierAware {

    long deleteIfNeeded(StoreService storeService, TopicName topicName, long minIndexedPosition) throws IOException;
}
