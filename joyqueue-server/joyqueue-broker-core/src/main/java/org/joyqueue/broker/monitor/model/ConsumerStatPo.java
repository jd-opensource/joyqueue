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
package org.joyqueue.broker.monitor.model;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * consumerstat po
 *
 * author: gaohaoxiang
 * date: 2018/10/12
 */
public class ConsumerStatPo extends BasePo {

    private RetryStatPo retryStat;
    private DeQueueStatPo deQueueStat;
    private Map<Integer, PartitionGroupStatPo> partitionGroupStatMap = Maps.newHashMap();

    public ConsumerStatPo(RetryStatPo retryStat, DeQueueStatPo deQueueStat) {
        this.retryStat = retryStat;
        this.deQueueStat = deQueueStat;
    }

    public ConsumerStatPo() {

    }

    public void setRetryStat(RetryStatPo retryStat) {
        this.retryStat = retryStat;
    }

    public RetryStatPo getRetryStat() {
        return retryStat;
    }

    public DeQueueStatPo getDeQueueStat() {
        return deQueueStat;
    }

    public void setDeQueueStat(DeQueueStatPo deQueueStat) {
        this.deQueueStat = deQueueStat;
    }

    public void setPartitionGroupStatMap(Map<Integer, PartitionGroupStatPo> partitionGroupStatMap) {
        this.partitionGroupStatMap = partitionGroupStatMap;
    }

    public Map<Integer, PartitionGroupStatPo> getPartitionGroupStatMap() {
        return partitionGroupStatMap;
    }
}