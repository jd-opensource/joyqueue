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
