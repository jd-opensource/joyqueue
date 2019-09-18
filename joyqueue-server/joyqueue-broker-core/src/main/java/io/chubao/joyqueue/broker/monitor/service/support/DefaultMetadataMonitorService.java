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
package io.chubao.joyqueue.broker.monitor.service.support;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.ExtensionPointLazy;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.monitor.service.MetadataMonitorService;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.InternalServiceProvider;
import io.chubao.joyqueue.nsr.MetadataSynchronizer;
import io.chubao.joyqueue.nsr.service.internal.AppTokenInternalService;
import io.chubao.joyqueue.nsr.service.internal.BrokerInternalService;
import io.chubao.joyqueue.nsr.service.internal.ConfigInternalService;
import io.chubao.joyqueue.nsr.service.internal.ConsumerInternalService;
import io.chubao.joyqueue.nsr.service.internal.DataCenterInternalService;
import io.chubao.joyqueue.nsr.service.internal.NamespaceInternalService;
import io.chubao.joyqueue.nsr.service.internal.OperationInternalService;
import io.chubao.joyqueue.nsr.service.internal.PartitionGroupInternalService;
import io.chubao.joyqueue.nsr.service.internal.PartitionGroupReplicaInternalService;
import io.chubao.joyqueue.nsr.service.internal.ProducerInternalService;
import io.chubao.joyqueue.nsr.service.internal.TopicInternalService;
import io.chubao.joyqueue.response.BooleanResponse;
import io.chubao.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DefaultMetadataMonitorService
 *
 * author: gaohaoxiang
 * date: 2019/2/11
 */
public class DefaultMetadataMonitorService implements MetadataMonitorService {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultMetadataMonitorService.class);

    private static final ExtensionPoint<InternalServiceProvider, String> SERVICE_PROVIDER_POINT = new ExtensionPointLazy<>(InternalServiceProvider.class);

    private ClusterManager clusterManager;
    private MetadataSynchronizer metadataSynchronizer;
    private String source;
    private String target;
    private int interval;
    private ExecutorService syncThreadPool;

    public DefaultMetadataMonitorService(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
        this.metadataSynchronizer = new MetadataSynchronizer();
    }

    @Override
    public TopicConfig getTopicMetadata(String topic, boolean isCluster) {
        TopicName topicName = TopicName.parse(topic);
        if (isCluster) {
            return clusterManager.getTopicConfig(topicName);
        } else {
            return clusterManager.getNameService().getTopicConfig(topicName);
        }
    }

    @Override
    public BooleanResponse getReadableResult(String topic, String app, String address) {
        TopicName topicName = TopicName.parse(topic);
        return clusterManager.checkReadable(topicName, app, address);
    }

    @Override
    public BooleanResponse getWritableResult(String topic, String app, String address) {
        TopicName topicName = TopicName.parse(topic);
        return clusterManager.checkWritable(topicName, app, address);
    }

    @Override
    public Consumer getConsumerMetadataByTopicAndApp(String topic, String app, boolean isCluster) {
        TopicName topicName = TopicName.parse(topic);
        if (isCluster) {
            return clusterManager.tryGetConsumer(topicName, app);
        } else {
            return clusterManager.getNameService().getConsumerByTopicAndApp(topicName, app);
        }
    }

    @Override
    public Producer getProducerMetadataByTopicAndApp(String topic, String app, boolean isCluster) {
        TopicName topicName = TopicName.parse(topic);
        if (isCluster) {
            return clusterManager.tryGetProducer(topicName, app);
        } else {
            return clusterManager.getNameService().getProducerByTopicAndApp(topicName, app);
        }
    }

    @Override
    public Object exportMetadata(String source) {
        InternalServiceProvider internalServiceProvider = SERVICE_PROVIDER_POINT.get(source);
        if (internalServiceProvider == null) {
            return "source not exist";
        }

        Map<String, Object> result = Maps.newHashMap();
        result.put("topic", internalServiceProvider.getService(TopicInternalService.class).getAll());
        result.put("partitionGroup", internalServiceProvider.getService(PartitionGroupInternalService.class).getAll());
        result.put("partitionGroupReplica", internalServiceProvider.getService(PartitionGroupReplicaInternalService.class).getAll());
        result.put("broker", internalServiceProvider.getService(BrokerInternalService.class).getAll());
        result.put("consumer", internalServiceProvider.getService(ConsumerInternalService.class).getAll());
        result.put("producer", internalServiceProvider.getService(ProducerInternalService.class).getAll());
        result.put("dataCenter", internalServiceProvider.getService(DataCenterInternalService.class).getAll());
        result.put("namespace", internalServiceProvider.getService(NamespaceInternalService.class).getAll());
        result.put("config", internalServiceProvider.getService(ConfigInternalService.class).getAll());
        result.put("appToken", internalServiceProvider.getService(AppTokenInternalService.class).getAll());
        return result;
    }

    @Override
    public Object syncMetadata(String source, String target, int interval) {
        this.source = source;
        this.target = target;
        this.interval = interval;

        if (interval != 0) {
            if (syncThreadPool == null) {
                syncThreadPool = Executors.newFixedThreadPool(1, new NamedThreadFactory("joyqueue-metadata-synchronizer"));
                syncThreadPool.execute(() -> {
                    while (true) {
                        InternalServiceProvider sourceInternalServiceProvider = SERVICE_PROVIDER_POINT.get(this.source);
                        InternalServiceProvider targetInternalServiceProvider = SERVICE_PROVIDER_POINT.get(this.target);

                        if (sourceInternalServiceProvider == null) {
                            logger.warn("source not exist");
                        }

                        if (targetInternalServiceProvider == null) {
                            logger.warn("target not exist");
                        }

                        Object result = metadataSynchronizer.sync(sourceInternalServiceProvider, targetInternalServiceProvider);
                        logger.info("sync result: {}", JSON.toJSONString(result));
                        try {
                            Thread.currentThread().sleep(this.interval);
                        } catch (InterruptedException e) {
                        }
                    }
                });
            }
            return "success";
        } else {
            InternalServiceProvider sourceInternalServiceProvider = SERVICE_PROVIDER_POINT.get(source);
            InternalServiceProvider targetInternalServiceProvider = SERVICE_PROVIDER_POINT.get(target);

            if (sourceInternalServiceProvider == null) {
                return "source not exist";
            }

            if (targetInternalServiceProvider == null) {
                return "target not exist";
            }

            return metadataSynchronizer.sync(sourceInternalServiceProvider, targetInternalServiceProvider);
        }
    }

    @Override
    public Object queryMetadata(String source, String operator, List<Object> params) {
        InternalServiceProvider sourceInternalServiceProvider = SERVICE_PROVIDER_POINT.get(source);
        if (sourceInternalServiceProvider == null) {
            return "source not exist";
        }
        OperationInternalService operationInternalService = sourceInternalServiceProvider.getService(OperationInternalService.class);
        return operationInternalService.query(operator, params);
    }

    @Override
    public Object insertMetadata(String source, String operator, List<Object> params) {
        InternalServiceProvider sourceInternalServiceProvider = SERVICE_PROVIDER_POINT.get(source);
        if (sourceInternalServiceProvider == null) {
            return "source not exist";
        }
        OperationInternalService operationInternalService = sourceInternalServiceProvider.getService(OperationInternalService.class);
        return operationInternalService.insert(operator, params);
    }

    @Override
    public Object updateMetadata(String source, String operator, List<Object> params) {
        InternalServiceProvider sourceInternalServiceProvider = SERVICE_PROVIDER_POINT.get(source);
        if (sourceInternalServiceProvider == null) {
            return "source not exist";
        }
        OperationInternalService operationInternalService = sourceInternalServiceProvider.getService(OperationInternalService.class);
        return operationInternalService.update(operator, params);
    }

    @Override
    public Object deleteMetadata(String source, String operator, List<Object> params) {
        InternalServiceProvider sourceInternalServiceProvider = SERVICE_PROVIDER_POINT.get(source);
        if (sourceInternalServiceProvider == null) {
            return "source not exist";
        }
        OperationInternalService operationInternalService = sourceInternalServiceProvider.getService(OperationInternalService.class);
        return operationInternalService.delete(operator, params);
    }
}