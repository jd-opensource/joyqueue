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
package io.chubao.joyqueue.store.file;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LiYue
 * Date: 2019/12/16
 */
public class Checkpoint {
    private int version;
    private long indexPosition;
    private Map<Short, Long> partitions;

    public Checkpoint() {};
    public Checkpoint(long indexPosition, Map<Short, Long> partitions) {
        this(indexPosition, partitions, 0);
    }

    public Checkpoint(long indexPosition, Map<Short, Long> partitions, int version) {
        this.indexPosition = indexPosition;
        this.partitions = new HashMap<>(partitions);
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public long getIndexPosition() {
        return indexPosition;
    }

    public Map<Short, Long> getPartitions() {
        return partitions;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setIndexPosition(long indexPosition) {
        this.indexPosition = indexPosition;
    }

    public void setPartitions(Map<Short, Long> partitions) {
        this.partitions = partitions;
    }
}
