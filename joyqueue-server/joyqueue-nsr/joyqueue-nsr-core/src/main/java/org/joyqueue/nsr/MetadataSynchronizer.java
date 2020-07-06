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
package org.joyqueue.nsr;

import com.google.common.collect.Maps;
import org.joyqueue.domain.AppToken;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.Config;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.DataCenter;
import org.joyqueue.domain.Namespace;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.Replica;
import org.joyqueue.domain.Topic;
import org.joyqueue.nsr.service.internal.AppTokenInternalService;
import org.joyqueue.nsr.service.internal.BrokerInternalService;
import org.joyqueue.nsr.service.internal.ConfigInternalService;
import org.joyqueue.nsr.service.internal.ConsumerInternalService;
import org.joyqueue.nsr.service.internal.DataCenterInternalService;
import org.joyqueue.nsr.service.internal.NamespaceInternalService;
import org.joyqueue.nsr.service.internal.PartitionGroupInternalService;
import org.joyqueue.nsr.service.internal.PartitionGroupReplicaInternalService;
import org.joyqueue.nsr.service.internal.ProducerInternalService;
import org.joyqueue.nsr.service.internal.TopicInternalService;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * MetadataSynchronizer
 * author: gaohaoxiang
 * date: 2019/9/6
 */
public class MetadataSynchronizer {

    protected static final Logger logger = LoggerFactory.getLogger(MetadataSynchronizer.class);

    public Object sync(InternalServiceProvider sourceInternalServiceProvider, InternalServiceProvider targetInternalServiceProvider, boolean onlyCompare) {
        Object syncBrokerResult = syncBroker(sourceInternalServiceProvider, targetInternalServiceProvider, onlyCompare);
        Object syncPartitionGroupResult = syncPartitionGroup(sourceInternalServiceProvider, targetInternalServiceProvider, onlyCompare);
        Object syncPartitionGroupReplicaResult = syncPartitionGroupReplica(sourceInternalServiceProvider, targetInternalServiceProvider, onlyCompare);
        Object syncTopicResult = syncTopic(sourceInternalServiceProvider, targetInternalServiceProvider, onlyCompare);
        Object syncConsumerResult = syncConsumer(sourceInternalServiceProvider, targetInternalServiceProvider, onlyCompare);
        Object syncProducerResult = syncProducer(sourceInternalServiceProvider, targetInternalServiceProvider, onlyCompare);
        Object syncDataCenterResult = syncDataCenter(sourceInternalServiceProvider, targetInternalServiceProvider, onlyCompare);
        Object syncNamespaceResult = syncNamespace(sourceInternalServiceProvider, targetInternalServiceProvider, onlyCompare);
        Object syncConfigResult = syncConfig(sourceInternalServiceProvider, targetInternalServiceProvider, onlyCompare);
        Object syncAppTokenResult = syncAppToken(sourceInternalServiceProvider, targetInternalServiceProvider, onlyCompare);

        Map<String, Object> result = Maps.newHashMap();
        result.put("broker", syncBrokerResult);
        result.put("partitionGroup", syncPartitionGroupResult);
        result.put("partitionGroupReplica", syncPartitionGroupReplicaResult);
        result.put("topic", syncTopicResult);
        result.put("consumer", syncConsumerResult);
        result.put("producer", syncProducerResult);
        result.put("dataCenter", syncDataCenterResult);
        result.put("namespace", syncNamespaceResult);
        result.put("config", syncConfigResult);
        result.put("appToken", syncAppTokenResult);
        return result;
    }

    protected Object syncTopic(InternalServiceProvider sourceInternalServiceProvider, InternalServiceProvider targetInternalServiceProvider, boolean onlyCompare) {
        TopicInternalService sourceService = sourceInternalServiceProvider.getService(TopicInternalService.class);
        TopicInternalService targetService = targetInternalServiceProvider.getService(TopicInternalService.class);

        return sync("topic", () -> {
            return sourceService.getAll();
        }, (item) -> {
            return sourceService.getTopicByCode(((Topic) item).getName().getNamespace(), ((Topic) item).getName().getCode());
        }, () -> {
            return targetService.getAll();
        }, (item) -> {
            return targetService.getTopicByCode(((Topic) item).getName().getNamespace(), ((Topic) item).getName().getCode());
        }, (item) -> {
            targetService.removeTopic((Topic) item);
        }, (item) -> {
            targetService.add((Topic) item);
        }, onlyCompare);
    }

    protected Object syncPartitionGroup(InternalServiceProvider sourceInternalServiceProvider, InternalServiceProvider targetInternalServiceProvider, boolean onlyCompare) {
        PartitionGroupInternalService sourceService = sourceInternalServiceProvider.getService(PartitionGroupInternalService.class);
        PartitionGroupInternalService targetService = targetInternalServiceProvider.getService(PartitionGroupInternalService.class);

        return sync("partitionGroup", () -> {
            return sourceService.getAll();
        }, (item) -> {
            return sourceService.getById(((PartitionGroup) item).getId());
        }, () -> {
            return targetService.getAll();
        }, (item) -> {
            return targetService.getById(((PartitionGroup) item).getId());
        }, (item) -> {
            targetService.delete(((PartitionGroup) item).getId());
        }, (item) -> {
            targetService.add((PartitionGroup) item);
        }, onlyCompare);
    }

    protected Object syncPartitionGroupReplica(InternalServiceProvider sourceInternalServiceProvider, InternalServiceProvider targetInternalServiceProvider, boolean onlyCompare) {
        PartitionGroupReplicaInternalService sourceService = sourceInternalServiceProvider.getService(PartitionGroupReplicaInternalService.class);
        PartitionGroupReplicaInternalService targetService = targetInternalServiceProvider.getService(PartitionGroupReplicaInternalService.class);

        return sync("replica", () -> {
            return sourceService.getAll();
        }, (item) -> {
            return sourceService.getById(((Replica) item).getId());
        }, () -> {
            return targetService.getAll();
        }, (item) -> {
            return targetService.getById(((Replica) item).getId());
        }, (item) -> {
            targetService.delete(((Replica) item).getId());
        }, (item) -> {
            targetService.add((Replica) item);
        }, onlyCompare);
    }

    protected Object syncBroker(InternalServiceProvider sourceInternalServiceProvider, InternalServiceProvider targetInternalServiceProvider, boolean onlyCompare) {
        BrokerInternalService sourceService = sourceInternalServiceProvider.getService(BrokerInternalService.class);
        BrokerInternalService targetService = targetInternalServiceProvider.getService(BrokerInternalService.class);

        return sync("broker", () -> {
            return sourceService.getAll();
        }, (item) -> {
            return sourceService.getById(((Broker) item).getId());
        }, () -> {
            return targetService.getAll();
        }, (item) -> {
            return targetService.getById(((Broker) item).getId());
        }, (item) -> {
            targetService.delete(((Broker) item).getId());
        }, (item) -> {
            targetService.add((Broker) item);
        }, onlyCompare);
    }

    protected Object syncConsumer(InternalServiceProvider sourceInternalServiceProvider, InternalServiceProvider targetInternalServiceProvider, boolean onlyCompare) {
        ConsumerInternalService sourceService = sourceInternalServiceProvider.getService(ConsumerInternalService.class);
        ConsumerInternalService targetService = targetInternalServiceProvider.getService(ConsumerInternalService.class);

        return sync("consumer", () -> {
            return sourceService.getAll();
        }, (item) -> {
            return sourceService.getById(((Consumer) item).getId());
        }, () -> {
            return targetService.getAll();
        }, (item) -> {
            return targetService.getById(((Consumer) item).getId());
        }, (item) -> {
            targetService.delete(((Consumer) item).getId());
        }, (item) -> {
            targetService.add((Consumer) item);
        }, onlyCompare);
    }

    protected Object syncProducer(InternalServiceProvider sourceInternalServiceProvider, InternalServiceProvider targetInternalServiceProvider, boolean onlyCompare) {
        ProducerInternalService sourceService = sourceInternalServiceProvider.getService(ProducerInternalService.class);
        ProducerInternalService targetService = targetInternalServiceProvider.getService(ProducerInternalService.class);

        return sync("producer", () -> {
            return sourceService.getAll();
        }, (item) -> {
            return sourceService.getById(((Producer) item).getId());
        }, () -> {
            return targetService.getAll();
        }, (item) -> {
            return targetService.getById(((Producer) item).getId());
        }, (item) -> {
            targetService.delete(((Producer) item).getId());
        }, (item) -> {
            targetService.add((Producer) item);
        }, onlyCompare);
    }

    protected Object syncDataCenter(InternalServiceProvider sourceInternalServiceProvider, InternalServiceProvider targetInternalServiceProvider, boolean onlyCompare) {
        DataCenterInternalService sourceService = sourceInternalServiceProvider.getService(DataCenterInternalService.class);
        DataCenterInternalService targetService = targetInternalServiceProvider.getService(DataCenterInternalService.class);

        return sync("datacenter", () -> {
            return sourceService.getAll();
        }, (item) -> {
            return sourceService.getById(((DataCenter) item).getId());
        }, () -> {
            return targetService.getAll();
        }, (item) -> {
            return targetService.getById(((DataCenter) item).getId());
        }, (item) -> {
            targetService.delete(((DataCenter) item).getId());
        }, (item) -> {
            targetService.add((DataCenter) item);
        }, onlyCompare);
    }

    protected Object syncNamespace(InternalServiceProvider sourceInternalServiceProvider, InternalServiceProvider targetInternalServiceProvider, boolean onlyCompare) {
        NamespaceInternalService sourceService = sourceInternalServiceProvider.getService(NamespaceInternalService.class);
        NamespaceInternalService targetService = targetInternalServiceProvider.getService(NamespaceInternalService.class);

        return sync("namespace", () -> {
            return sourceService.getAll();
        }, (item) -> {
            return sourceService.getById(((Namespace) item).getCode());
        }, () -> {
            return targetService.getAll();
        }, (item) -> {
            return targetService.getById(((Namespace) item).getCode());
        }, (item) -> {
            targetService.delete(((Namespace) item).getCode());
        }, (item) -> {
            targetService.add((Namespace) item);
        }, onlyCompare);
    }

    protected Object syncConfig(InternalServiceProvider sourceInternalServiceProvider, InternalServiceProvider targetInternalServiceProvider, boolean onlyCompare) {
        ConfigInternalService sourceService = sourceInternalServiceProvider.getService(ConfigInternalService.class);
        ConfigInternalService targetService = targetInternalServiceProvider.getService(ConfigInternalService.class);

        return sync("config", () -> {
            return sourceService.getAll();
        }, (item) -> {
            return sourceService.getById(((Config) item).getId());
        }, () -> {
            return targetService.getAll();
        }, (item) -> {
            return targetService.getById(((Config) item).getId());
        }, (item) -> {
            targetService.delete(((Config) item).getId());
        }, (item) -> {
            targetService.add((Config) item);
        }, onlyCompare);
    }

    protected Object syncAppToken(InternalServiceProvider sourceInternalServiceProvider, InternalServiceProvider targetInternalServiceProvider, boolean onlyCompare) {
        AppTokenInternalService sourceService = sourceInternalServiceProvider.getService(AppTokenInternalService.class);
        AppTokenInternalService targetService = targetInternalServiceProvider.getService(AppTokenInternalService.class);

        return sync("apptoken", () -> {
            return sourceService.getAll();
        }, (item) -> {
            return sourceService.getById(((AppToken) item).getId());
        }, () -> {
            return targetService.getAll();
        }, (item) -> {
            return targetService.getById(((AppToken) item).getId());
        }, (item) -> {
            targetService.delete(((AppToken) item).getId());
        }, (item) -> {
            targetService.add((AppToken) item);
        }, onlyCompare);
    }

    protected Object sync(String name, Callable<List> sourceAllCallable, Function<Object, Object> sourceFindFunction
            , Callable<List> targetAllCallable, Function<Object, Object> targetFindFunction
            , java.util.function.Consumer<Object> targetDeleteConsumer, java.util.function.Consumer<Object> targetAddConsumer, boolean onlyCompare) {
        try {
            int success = 0;
            int failure = 0;
            int delete = 0;

            long sourceStartTime = SystemClock.now();
            List sourceList = sourceAllCallable.call();
            logger.info("get {} source, data: {}, time: {}", name, sourceList.size(), SystemClock.now() - sourceStartTime);

            for (int i = 0; i < sourceList.size(); i++) {
                Object sourceItem = sourceList.get(i);
                Object targetItem = targetFindFunction.apply(sourceItem);
                if (targetItem == null) {
                    logger.info("not exist, source: {}, target: {}", sourceItem, targetItem);
                    if (!onlyCompare) {
                        targetAddConsumer.accept(sourceItem);
                    }
                    success++;
                } else {
                    if (!sourceItem.equals(targetItem)) {
                        logger.info("not equals, source: {}, target: {}", sourceItem, targetItem);
                        if (!onlyCompare) {
                            targetDeleteConsumer.accept(sourceItem);
                            targetAddConsumer.accept(sourceItem);
                        }
                        success++;
                    } else {
                        failure++;
                    }
                }

                if (i % 10 == 0) {
                    logger.info("sync {}, index: {}", name, i);
                }
            }

            long targetStartTime = SystemClock.now();
            List targetList = targetAllCallable.call();
            logger.info("get {} target, data: {}, time: {}", name, targetList.size(), SystemClock.now() - targetStartTime);

            for (int i = 0; i < targetList.size(); i++) {
                Object targetItem = targetList.get(i);
                Object sourceItem = sourceFindFunction.apply(targetItem);
                if (sourceItem == null) {
                    logger.info("source not exist, target: {}", targetItem);
                    if (!onlyCompare) {
                        targetDeleteConsumer.accept(targetItem);
                    }
                    delete++;
                    logger.info("delete {}, index: {}", name, i);
                }
            }
            return String.format("success %d, failure: %d, delete: %d", success, failure, delete);
        } catch (Exception e) {
            logger.error("sync exception", e);
            return null;
        }
    }
}