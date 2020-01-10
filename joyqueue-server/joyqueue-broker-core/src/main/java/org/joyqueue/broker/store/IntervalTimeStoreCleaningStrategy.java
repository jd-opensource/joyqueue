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
package org.joyqueue.broker.store;

import org.joyqueue.broker.config.BrokerStoreConfig;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.time.SystemClock;

import java.io.IOException;
import java.util.Map;

/**
 * @author majun8
 */
public class IntervalTimeStoreCleaningStrategy implements StoreCleaningStrategy {
    private long maxIntervalTime;
    private boolean keepUnconsumed = true;

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
                deletedSize = partitionGroupStore.clean(targetDeleteTimeline, partitionAckMap, keepUnconsumed);
                totalDeletedSize += deletedSize;
            } while (deletedSize > 0L);
        }

        return totalDeletedSize;
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        BrokerStoreConfig brokerStoreConfig = new BrokerStoreConfig(supplier);
        this.maxIntervalTime = brokerStoreConfig.getMaxStoreTime();
        this.keepUnconsumed = brokerStoreConfig.keepUnconsumed();
    }
}
