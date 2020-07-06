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
package org.joyqueue.broker.monitor.converter;

import com.google.common.collect.Maps;
import org.joyqueue.broker.monitor.model.AppStatPo;
import org.joyqueue.broker.monitor.model.BrokerStatPo;
import org.joyqueue.broker.monitor.model.ConsumerStatPo;
import org.joyqueue.broker.monitor.model.DeQueueStatPo;
import org.joyqueue.broker.monitor.model.EnQueueStatPo;
import org.joyqueue.broker.monitor.model.PartitionGroupStatPo;
import org.joyqueue.broker.monitor.model.PartitionStatPo;
import org.joyqueue.broker.monitor.model.ProducerStatPo;
import org.joyqueue.broker.monitor.model.ReplicationStatPo;
import org.joyqueue.broker.monitor.model.RetryStatPo;
import org.joyqueue.broker.monitor.model.TopicStatPo;
import org.joyqueue.broker.monitor.stat.AppStat;
import org.joyqueue.broker.monitor.stat.BrokerStat;
import org.joyqueue.broker.monitor.stat.PartitionGroupStat;
import org.joyqueue.broker.monitor.stat.PartitionStat;
import org.joyqueue.broker.monitor.stat.RetryStat;
import org.joyqueue.broker.monitor.stat.TopicStat;

import java.util.Map;

/**
 * converter
 *
 * author: gaohaoxiang
 * date: 2018/10/12
 */
// TODO 优化，拆分小方法
public class BrokerStatConverter {

    public static BrokerStatPo convertToPo(BrokerStat brokerStat) {
        BrokerStatPo brokerStatPo = new BrokerStatPo();
        Map<String, TopicStatPo> topicStatPoMap = Maps.newHashMap();
        brokerStatPo.setVersion(brokerStat.VERSION);
        brokerStatPo.setTopicStatMap(topicStatPoMap);
        brokerStatPo.setEnQueueStat(new EnQueueStatPo(brokerStat.getEnQueueStat().getTotal(), brokerStat.getEnQueueStat().getTotalSize()));
        brokerStatPo.setDeQueueStat(new DeQueueStatPo(brokerStat.getDeQueueStat().getTotal(), brokerStat.getDeQueueStat().getTotalSize()));
        brokerStatPo.setReplicationStat(new ReplicationStatPo(
                new EnQueueStatPo(brokerStat.getReplicationStat().getReplicaStat().getTotal(), brokerStat.getReplicationStat().getReplicaStat().getTotalSize()),
                new EnQueueStatPo(brokerStat.getReplicationStat().getAppendStat().getTotal(), brokerStat.getReplicationStat().getAppendStat().getTotalSize())
        ));

        // topic
        for (Map.Entry<String, TopicStat> topicStatEntry : brokerStat.getTopicStats().entrySet()) {
            TopicStat topicStat = topicStatEntry.getValue();
            TopicStatPo topicStatPo = new TopicStatPo();
            Map<String, AppStatPo> appStatPoMap = Maps.newHashMap();
            topicStatPoMap.put(topicStat.getTopic(), topicStatPo);

            topicStatPo.setAppStatMap(appStatPoMap);
            topicStatPo.setEnQueueStat(new EnQueueStatPo(topicStat.getEnQueueStat().getTotal(), topicStat.getEnQueueStat().getTotalSize()));
            topicStatPo.setDeQueueStat(new DeQueueStatPo(topicStat.getDeQueueStat().getTotal(), topicStat.getDeQueueStat().getTotalSize()));

            // partition group

            for (Map.Entry<Integer, PartitionGroupStat> partitionGroupStatEntry : topicStat.getPartitionGroupStatMap().entrySet()) {
                PartitionGroupStatPo partitionGroupStatPo = convertPartitionGroupStatPo(partitionGroupStatEntry.getValue());
                topicStatPo.getPartitionGroupStatMap().put(partitionGroupStatEntry.getKey(), partitionGroupStatPo);
            }

            // app

            for (Map.Entry<String, AppStat> appStatEntry : topicStat.getAppStats().entrySet()) {
                AppStat appStat = appStatEntry.getValue();
                RetryStat retryStat = appStat.getConsumerStat().getRetryStat();
                AppStatPo appStatPo = new AppStatPo();
                Map<Integer, PartitionGroupStatPo> partitionGroupStatPoMap = Maps.newHashMap();

                appStatPoMap.put(appStatEntry.getKey(), appStatPo);
                appStatPo.setPartitionGroupStatMap(partitionGroupStatPoMap);
                appStatPo.setConsumerStat(new ConsumerStatPo(new RetryStatPo(retryStat.getTotal().getCount(), retryStat.getSuccess().getCount(), retryStat.getFailure().getCount()),
                        new DeQueueStatPo(appStat.getConsumerStat().getDeQueueStat().getTotal(), appStat.getConsumerStat().getDeQueueStat().getTotalSize())));
                appStatPo.setProducerStat(new ProducerStatPo(new EnQueueStatPo(appStat.getProducerStat().getEnQueueStat().getTotal(), appStat.getProducerStat().getEnQueueStat().getTotalSize())));

                // consumer

                for (Map.Entry<Integer, PartitionGroupStat> partitionGroupStatEntry : appStat.getConsumerStat().getPartitionGroupStatMap().entrySet()) {
                    PartitionGroupStat partitionGroupStat = partitionGroupStatEntry.getValue();
                    PartitionGroupStatPo partitionGroupStatPo = convertPartitionGroupStatPo(partitionGroupStat);
                    appStatPo.getConsumerStat().getPartitionGroupStatMap().put(partitionGroupStatEntry.getKey(), partitionGroupStatPo);
                }

                // producer

                for (Map.Entry<Integer, PartitionGroupStat> partitionGroupStatEntry : appStat.getProducerStat().getPartitionGroupStatMap().entrySet()) {
                    PartitionGroupStat partitionGroupStat = partitionGroupStatEntry.getValue();
                    PartitionGroupStatPo partitionGroupStatPo = convertPartitionGroupStatPo(partitionGroupStat);
                    appStatPo.getProducerStat().getPartitionGroupStatMap().put(partitionGroupStatEntry.getKey(), partitionGroupStatPo);
                }

                // partition group
                for (Map.Entry<Integer, PartitionGroupStat> partitionGroupStatEntry : appStat.getPartitionGroupStatMap().entrySet()) {
                    PartitionGroupStat partitionGroupStat = partitionGroupStatEntry.getValue();
                    PartitionGroupStatPo partitionGroupStatPo = convertPartitionGroupStatPo(partitionGroupStat);
                    partitionGroupStatPoMap.put(partitionGroupStatEntry.getKey(), partitionGroupStatPo);
                }
            }
        }

        return brokerStatPo;
    }

    protected static PartitionGroupStatPo convertPartitionGroupStatPo(PartitionGroupStat partitionGroupStat) {
        PartitionGroupStatPo partitionGroupStatPo = new PartitionGroupStatPo();
        Map<Short, PartitionStatPo> partitionStatPoMap = Maps.newHashMap();

        partitionGroupStatPo.setPartitionStatMap(partitionStatPoMap);
        partitionGroupStatPo.setEnQueueStat(new EnQueueStatPo(partitionGroupStat.getEnQueueStat().getTotal(), partitionGroupStat.getEnQueueStat().getTotalSize()));
        partitionGroupStatPo.setDeQueueStat(new DeQueueStatPo(partitionGroupStat.getDeQueueStat().getTotal(), partitionGroupStat.getDeQueueStat().getTotalSize()));

        // partition
        for (Map.Entry<Short, PartitionStat> partitionStatEntry : partitionGroupStat.getPartitionStatMap().entrySet()) {
            PartitionStat partitionStat = partitionStatEntry.getValue();
            PartitionStatPo partitionStatPo = new PartitionStatPo();

            partitionStatPo.setPartition(partitionStatEntry.getKey());
            partitionStatPo.setEnQueueStat(new EnQueueStatPo(partitionStat.getEnQueueStat().getTotal(), partitionStat.getEnQueueStat().getTotalSize()));
            partitionStatPo.setDeQueueStat(new DeQueueStatPo(partitionStat.getDeQueueStat().getTotal(), partitionStat.getDeQueueStat().getTotalSize()));

            partitionStatPoMap.put(partitionStatEntry.getKey(), partitionStatPo);
        }

        partitionGroupStatPo.setReplicationStat(new ReplicationStatPo(
                new EnQueueStatPo(partitionGroupStat.getReplicationStat().getReplicaStat().getTotal(), partitionGroupStat.getReplicationStat().getReplicaStat().getTotalSize()),
                new EnQueueStatPo(partitionGroupStat.getReplicationStat().getAppendStat().getTotal(), partitionGroupStat.getReplicationStat().getAppendStat().getTotalSize())
        ));
        return partitionGroupStatPo;
    }

    public static BrokerStat convert(BrokerStatPo brokerStatPo) {
        BrokerStat brokerStat = new BrokerStat(brokerStatPo.getBrokerId());
        brokerStat.getEnQueueStat().setTotal(brokerStatPo.getEnQueueStat().getTotal());
        brokerStat.getDeQueueStat().setTotalSize(brokerStatPo.getEnQueueStat().getTotalSize());
        brokerStat.getReplicationStat().getReplicaStat().setTotal(brokerStatPo.getReplicationStat().getReplicaStat().getTotal());
        brokerStat.getReplicationStat().getReplicaStat().setTotalSize(brokerStatPo.getReplicationStat().getReplicaStat().getTotalSize());
        brokerStat.getReplicationStat().getAppendStat().setTotal(brokerStatPo.getReplicationStat().getAppendStat().getTotal());
        brokerStat.getReplicationStat().getAppendStat().setTotalSize(brokerStatPo.getReplicationStat().getAppendStat().getTotalSize());


        // topic
        for (Map.Entry<String, TopicStatPo> topicStatPoEntry : brokerStatPo.getTopicStatMap().entrySet()) {
            TopicStatPo topicStatPo = topicStatPoEntry.getValue();
            TopicStat topicStat = brokerStat.getOrCreateTopicStat(topicStatPoEntry.getKey());

            topicStat.getEnQueueStat().setTotal(topicStatPo.getEnQueueStat().getTotal());
            topicStat.getEnQueueStat().setTotalSize(topicStatPo.getEnQueueStat().getTotalSize());
            topicStat.getDeQueueStat().setTotal(topicStatPo.getDeQueueStat().getTotal());
            topicStat.getDeQueueStat().setTotalSize(topicStatPo.getDeQueueStat().getTotalSize());

            // partition group

            for (Map.Entry<Integer, PartitionGroupStatPo> partitionGroupStatPoEntry : topicStatPo.getPartitionGroupStatMap().entrySet()) {
                PartitionGroupStatPo partitionGroupStatPo = partitionGroupStatPoEntry.getValue();
                PartitionGroupStat partitionGroupStat = topicStat.getOrCreatePartitionGroupStat(partitionGroupStatPoEntry.getKey());
                partitionGroupStat = convert(partitionGroupStat, partitionGroupStatPo);
            }

            // app
            for (Map.Entry<String, AppStatPo> appStatPoEntry : topicStatPo.getAppStatMap().entrySet()) {
                AppStatPo appStatPo = appStatPoEntry.getValue();
                RetryStatPo retryStatPo = appStatPo.getConsumerStat().getRetryStat();
                AppStat appStat = topicStat.getOrCreateAppStat(appStatPoEntry.getKey());

                appStat.getProducerStat().getEnQueueStat().setTotal(appStatPo.getProducerStat().getEnQueueStat().getTotal());
                appStat.getProducerStat().getEnQueueStat().setTotalSize(appStatPo.getProducerStat().getEnQueueStat().getTotalSize());
                appStat.getConsumerStat().getDeQueueStat().setTotal(appStatPo.getConsumerStat().getDeQueueStat().getTotal());
                appStat.getConsumerStat().getDeQueueStat().setTotalSize(appStatPo.getConsumerStat().getDeQueueStat().getTotalSize());
                appStat.getConsumerStat().getRetryStat().getTotal().setCount(retryStatPo.getTotal());
                appStat.getConsumerStat().getRetryStat().getSuccess().setCount(retryStatPo.getSuccess());
                appStat.getConsumerStat().getRetryStat().getFailure().setCount(retryStatPo.getFailure());

                // consumer

                for (Map.Entry<Integer, PartitionGroupStatPo> partitionGroupStatPoEntry : appStatPo.getConsumerStat().getPartitionGroupStatMap().entrySet()) {
                    PartitionGroupStatPo partitionGroupStatPo = partitionGroupStatPoEntry.getValue();
                    PartitionGroupStat partitionGroupStat = appStat.getConsumerStat().getOrCreatePartitionGroupStat(partitionGroupStatPoEntry.getKey());
                    partitionGroupStat = convert(partitionGroupStat, partitionGroupStatPo);
                }

                // producer

                for (Map.Entry<Integer, PartitionGroupStatPo> partitionGroupStatPoEntry : appStatPo.getProducerStat().getPartitionGroupStatMap().entrySet()) {
                    PartitionGroupStatPo partitionGroupStatPo = partitionGroupStatPoEntry.getValue();
                    PartitionGroupStat partitionGroupStat = appStat.getProducerStat().getOrCreatePartitionGroupStat(partitionGroupStatPoEntry.getKey());
                    partitionGroupStat = convert(partitionGroupStat, partitionGroupStatPo);
                }

                // partition group
                for (Map.Entry<Integer, PartitionGroupStatPo> partitionGroupStatPoEntry : appStatPo.getPartitionGroupStatMap().entrySet()) {
                    PartitionGroupStatPo partitionGroupStatPo = partitionGroupStatPoEntry.getValue();
                    PartitionGroupStat partitionGroupStat = appStat.getOrCreatePartitionGroupStat(partitionGroupStatPoEntry.getKey());
                    partitionGroupStat = convert(partitionGroupStat, partitionGroupStatPo);
                }
            }
        }

        return brokerStat;
    }

    protected static PartitionGroupStat convert(PartitionGroupStat partitionGroupStat, PartitionGroupStatPo partitionGroupStatPo) {
        partitionGroupStat.getEnQueueStat().setTotal(partitionGroupStatPo.getEnQueueStat().getTotal());
        partitionGroupStat.getEnQueueStat().setTotalSize(partitionGroupStatPo.getEnQueueStat().getTotalSize());
        partitionGroupStat.getDeQueueStat().setTotal(partitionGroupStatPo.getDeQueueStat().getTotal());
        partitionGroupStat.getDeQueueStat().setTotalSize(partitionGroupStatPo.getDeQueueStat().getTotalSize());
        partitionGroupStat.getReplicationStat().getReplicaStat().setTotal(partitionGroupStatPo.getReplicationStat().getReplicaStat().getTotal());
        partitionGroupStat.getReplicationStat().getReplicaStat().setTotalSize(partitionGroupStatPo.getReplicationStat().getReplicaStat().getTotalSize());
        partitionGroupStat.getReplicationStat().getAppendStat().setTotal(partitionGroupStatPo.getReplicationStat().getAppendStat().getTotal());
        partitionGroupStat.getReplicationStat().getAppendStat().setTotalSize(partitionGroupStatPo.getReplicationStat().getAppendStat().getTotalSize());

        // partition
        for (Map.Entry<Short, PartitionStatPo> partitionStatPoEntry : partitionGroupStatPo.getPartitionStatMap().entrySet()) {
            PartitionStatPo partitionStatPo = partitionStatPoEntry.getValue();
            PartitionStat partitionStat = partitionGroupStat.getOrCreatePartitionStat(partitionStatPo.getPartition());

            partitionStat.getEnQueueStat().setTotal(partitionStatPo.getEnQueueStat().getTotal());
            partitionStat.getEnQueueStat().setTotalSize(partitionStatPo.getEnQueueStat().getTotalSize());
            partitionStat.getDeQueueStat().setTotal(partitionStatPo.getDeQueueStat().getTotal());
            partitionStat.getDeQueueStat().setTotalSize(partitionStatPo.getDeQueueStat().getTotalSize());
        }

        return partitionGroupStat;
    }
}