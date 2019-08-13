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
package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.exception.JoyQueueCode;

import java.util.Collections;
import java.util.List;

/**
 * FetchAssignedPartitionAckData
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/17
 */
public class FetchAssignedPartitionAckData {

    private List<Short> partitions;
    private JoyQueueCode code;

    public FetchAssignedPartitionAckData() {

    }

    public FetchAssignedPartitionAckData(JoyQueueCode code) {
        this.partitions = Collections.emptyList();
        this.code = code;
    }

    public FetchAssignedPartitionAckData(List<Short> partitions, JoyQueueCode code) {
        this.partitions = partitions;
        this.code = code;
    }

    public void setPartitions(List<Short> partitions) {
        this.partitions = partitions;
    }

    public List<Short> getPartitions() {
        return partitions;
    }

    public void setCode(JoyQueueCode code) {
        this.code = code;
    }

    public JoyQueueCode getCode() {
        return code;
    }
}