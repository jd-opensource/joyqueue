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
package io.chubao.joyqueue.nsr;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.domain.Namespace;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.Topic;
import io.chubao.joyqueue.nsr.service.internal.AppTokenInternalService;
import io.chubao.joyqueue.nsr.service.internal.BrokerInternalService;
import io.chubao.joyqueue.nsr.service.internal.ConfigInternalService;
import io.chubao.joyqueue.nsr.service.internal.ConsumerInternalService;
import io.chubao.joyqueue.nsr.service.internal.DataCenterInternalService;
import io.chubao.joyqueue.nsr.service.internal.NamespaceInternalService;
import io.chubao.joyqueue.nsr.service.internal.PartitionGroupInternalService;
import io.chubao.joyqueue.nsr.service.internal.PartitionGroupReplicaInternalService;
import io.chubao.joyqueue.nsr.service.internal.ProducerInternalService;
import io.chubao.joyqueue.nsr.service.internal.TopicInternalService;
import io.chubao.joyqueue.toolkit.time.SystemClock;
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
            return targetService.getTopicByCode(((Topic) item).getName().getNamespace(), ((Topic) item).getName().getCode());
        }, (item) -> {
            targetService.update((Topic) item);
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

        return sync("dataCenter", () -> {
            return sourceService.getAll();
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
            return targetService.getByCode(((Namespace) item).getCode());
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
            return targetService.getById(((AppToken) item).getId());
        }, (item) -> {
            targetService.delete(((AppToken) item).getId());
        }, (item) -> {
            targetService.add((AppToken) item);
        }, onlyCompare);
    }

    protected Object sync(String name, Callable<List> getAllCallable, Function<Object, Object> findFunction
            , java.util.function.Consumer<Object> deleteConsumer, java.util.function.Consumer<Object> addConsumer, boolean onlyCompare) {
        try {
            int success = 0;
            int failure = 0;

            long startTime = SystemClock.now();
            List source = getAllCallable.call();
            logger.info("get {} source, data: {}, time: {}", name, source.size(), SystemClock.now() - startTime);

            for (int i = 0; i < source.size(); i++) {
                Object item = source.get(i);
                Object targetItem = findFunction.apply(item);
                if (targetItem != null) {
                    if (!item.equals(targetItem)) {
                        logger.info("not equals, source: {}, target: {}", item, targetItem);
                        deleteConsumer.accept(item);
                        addConsumer.accept(item);
                        success++;
                    } else {
                        failure++;
                    }
                } else {
                    try {
                        if (!onlyCompare) {
                            addConsumer.accept(item);
                        }
                        success++;
                    } catch (Exception e) {
                        logger.error("add target {} error, data: {}, message: {}", JSON.toJSONString(item), e.toString());
                        logger.debug("add target {} error, data: {}", JSON.toJSONString(item), e);
                        failure++;
                    }
                }

                if (i % 10 == 0) {
                    logger.info("sync {}, index: {}", name, i);
                }
            }
            return String.format("success %s, failure: %s", success, failure);
        } catch (Exception e) {
            logger.error("sync exception", e);
            return null;
        }
    }
}