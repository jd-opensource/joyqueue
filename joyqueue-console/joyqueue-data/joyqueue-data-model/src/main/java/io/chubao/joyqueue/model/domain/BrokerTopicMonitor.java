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
package io.chubao.joyqueue.model.domain;

import io.chubao.joyqueue.manage.PartitionGroupMetric;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/3/13.
 */
public class BrokerTopicMonitor {
    private String topic;
    private long storageSize;
    private List<BrokerTopicMonitorRecord> brokerTopicMonitorRecordList;
    private List<PartitionGroupMetric> partitionGroupMetricList;
    /**
     * broker partition group leader count
     **/
    private int partitionGroupLeaders;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<BrokerTopicMonitorRecord> getBrokerTopicMonitorRecordList() {
        return brokerTopicMonitorRecordList;
    }

    public void setBrokerTopicMonitorRecordList(List<BrokerTopicMonitorRecord> brokerTopicMonitorRecordList) {
        this.brokerTopicMonitorRecordList = brokerTopicMonitorRecordList;
    }

    public List<PartitionGroupMetric> getPartitionGroupMetricList() {
        return partitionGroupMetricList;
    }

    public void setPartitionGroupMetricList(List<PartitionGroupMetric> partitionGroupMetricList) {
        this.partitionGroupMetricList = partitionGroupMetricList;
    }

    public long getStorageSize() {
        return storageSize;
    }

    public void setStorageSize(long storageSize) {
        this.storageSize = storageSize;
    }

    public int getPartitionGroupLeaders() {
        return partitionGroupLeaders;
    }

    public void setPartitionGroupLeaders(int partitionGroupLeaders) {
        this.partitionGroupLeaders = partitionGroupLeaders;
    }
}
