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

import org.joyqueue.domain.TopicConfig;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.time.SystemClock;

import java.io.IOException;
import java.util.Map;

/**
 * @author majun8
 */
public class IntervalTimeStoreCleaningStrategy extends AbstractStoreCleaningStrategy{
    public IntervalTimeStoreCleaningStrategy() {

    }

    @Override
    public long deleteIfNeeded(PartitionGroupStore partitionGroupStore, Map<Short, Long> partitionAckMap, TopicConfig topicConfig) throws IOException {
        long currentTimestamp = SystemClock.now();
        long targetDeleteTimeline = currentTimestamp -storeLogMaxTime(topicConfig) ;

        long totalDeletedSize = 0L;  // 总共删除长度
        long deletedSize = 0L;

        if (partitionGroupStore != null) {
            do {
                deletedSize = partitionGroupStore.clean(targetDeleteTimeline, partitionAckMap, keepUnconsumed(topicConfig));
                totalDeletedSize += deletedSize;
            } while (deletedSize > 0L);
        }

        return totalDeletedSize;
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        super.setSupplier(supplier);
    }
}
