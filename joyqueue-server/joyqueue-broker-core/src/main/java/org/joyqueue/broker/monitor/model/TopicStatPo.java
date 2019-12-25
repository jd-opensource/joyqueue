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
 * 主题po
 *
 * author: gaohaoxiang
 * date: 2018/10/12
 */
public class TopicStatPo extends BasePo {

    private EnQueueStatPo enQueueStat;
    private DeQueueStatPo deQueueStat;
    private Map<String, AppStatPo> appStatMap = Maps.newHashMap();
    private Map<Integer, PartitionGroupStatPo> partitionGroupStatMap = Maps.newHashMap();

    public EnQueueStatPo getEnQueueStat() {
        return enQueueStat;
    }

    public void setEnQueueStat(EnQueueStatPo enQueueStat) {
        this.enQueueStat = enQueueStat;
    }

    public DeQueueStatPo getDeQueueStat() {
        return deQueueStat;
    }

    public void setDeQueueStat(DeQueueStatPo deQueueStat) {
        this.deQueueStat = deQueueStat;
    }

    public Map<String, AppStatPo> getAppStatMap() {
        return appStatMap;
    }

    public void setAppStatMap(Map<String, AppStatPo> appStatMap) {
        this.appStatMap = appStatMap;
    }

    public void setPartitionGroupStatMap(Map<Integer, PartitionGroupStatPo> partitionGroupStatMap) {
        this.partitionGroupStatMap = partitionGroupStatMap;
    }

    public Map<Integer, PartitionGroupStatPo> getPartitionGroupStatMap() {
        return partitionGroupStatMap;
    }
}