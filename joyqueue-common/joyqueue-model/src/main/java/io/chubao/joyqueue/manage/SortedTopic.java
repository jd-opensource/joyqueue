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
package io.chubao.joyqueue.manage;

/**
 *  排序的 topic
 **/
public class SortedTopic {
    private String topic;
    private long value;
    private int order;
    // partition group leader count
    private int partitionGroupLeaders;
    private int partitionGroups;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getPartitionGroupLeaders() {
        return partitionGroupLeaders;
    }

    public void setPartitionGroupLeaders(int partitionGroupLeaders) {
        this.partitionGroupLeaders = partitionGroupLeaders;
    }

    public int getPartitionGroups() {
        return partitionGroups;
    }

    public void setPartitionGroups(int partitionGroups) {
        this.partitionGroups = partitionGroups;
    }
}
