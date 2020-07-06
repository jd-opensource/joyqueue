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
package org.joyqueue.nsr.network.command;

import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Topic;
import org.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/2/21
 */
public class AddTopic extends JoyQueuePayload {
    private Topic topic;
    private List<PartitionGroup> partitionGroups;

    public AddTopic topic(Topic topic){
        this.topic = topic;
        return this;
    }
    public AddTopic partitiionGroups(List<PartitionGroup> partitionGroups){
        this.partitionGroups = partitionGroups;
        return this;
    }

    public Topic getTopic() {
        return topic;
    }

    public List<PartitionGroup> getPartitionGroups() {
        return partitionGroups;
    }

    @Override
    public int type() {
        return NsrCommandType.ADD_TOPIC;
    }
}
