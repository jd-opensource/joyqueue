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
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.toolkit.config.PropertySupplier;

import java.io.IOException;
import java.util.Map;

/**
 * @author majun8
 */
public class FixedSizeStoreCleaningStrategy extends AbstractStoreCleaningStrategy {

    private long maxStorageSize;
    public FixedSizeStoreCleaningStrategy() {

    }

    @Override
    public long deleteIfNeeded(PartitionGroupStore partitionGroupStore, Map<Short, Long> partitionAckMap, TopicConfig topicConfig) throws IOException {
        long totalDeletedSize = 0L;  // 总共删除长度

        if (partitionGroupStore != null) {
            long currentTotalStorageSize = partitionGroupStore.getTotalPhysicalStorageSize();
            if( maxStorageSize < currentTotalStorageSize ) {

                long targetDeleteSize = currentTotalStorageSize - maxStorageSize;  // 目标删除长度

                long lastDeletedSize;  // 上一次删除长度
                do {
                    lastDeletedSize = partitionGroupStore.clean(0, partitionAckMap, keepUnconsumed(topicConfig));
                } while (lastDeletedSize > 0L && (totalDeletedSize += lastDeletedSize) < targetDeleteSize);
            }
        }

        return totalDeletedSize;
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        super.setSupplier(supplier);
        BrokerStoreConfig brokerStoreConfig = new BrokerStoreConfig(supplier);
        this.maxStorageSize = brokerStoreConfig.getMaxStoreSize();
    }
}
