/**
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
package com.jd.journalq.broker.store;

import com.jd.journalq.broker.config.BrokerStoreConfig;
import com.jd.journalq.store.PartitionGroupStore;
import com.jd.journalq.toolkit.config.PropertySupplier;

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
