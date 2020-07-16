package org.joyqueue.broker.cluster;

import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.joyqueue.broker.Plugins;
import org.joyqueue.broker.cluster.config.ClusterConfig;
import org.joyqueue.broker.cluster.config.ClusterConfigKey;
import org.joyqueue.broker.cluster.entry.ClusterNode;
import org.joyqueue.broker.cluster.entry.ClusterPartitionGroup;
import org.joyqueue.broker.cluster.entry.SplittedCluster;
import org.joyqueue.broker.cluster.helper.ClusterSplitHelper;
import org.joyqueue.broker.event.BrokerEventBus;
import org.joyqueue.broker.network.command.GetPartitionGroupClusterRequest;
import org.joyqueue.broker.network.command.GetPartitionGroupClusterResponse;
import org.joyqueue.broker.network.support.BrokerTransportClientFactory;
import org.joyqueue.config.BrokerConfigKey;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.Subscription;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.monitor.PointTracer;
import org.joyqueue.monitor.TraceStat;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.network.transport.command.JoyQueueCommand;
import org.joyqueue.network.transport.config.TransportConfigSupport;
import org.joyqueue.network.transport.session.session.TransportSession;
import org.joyqueue.network.transport.session.session.TransportSessionManager;
import org.joyqueue.network.transport.session.session.config.TransportSessionConfig;
import org.joyqueue.nsr.NameService;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ClusterNameService
 * author: gaohaoxiang
 * date: 2020/3/25
 */
public class ClusterNameService extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(ClusterNameService.class);

    private Broker broker;
    private NameService nameService;
    private BrokerEventBus eventBus;
    private TransportSessionManager transportSessionManager;
    private PropertySupplier propertySupplier;
    private ClusterConfig config;
    private ClusterNameServiceCache cache;

    private PointTracer tracer;
    private ClusterNodeManager clusterNodeManager;
    private ClusterNameServiceExecutorService clusterNameServiceExecutorService;

    public ClusterNameService(NameService nameService, BrokerEventBus eventBus, PropertySupplier propertySupplier) {
        this.nameService = nameService;
        this.eventBus = eventBus;
        this.propertySupplier = propertySupplier;
        this.config = new ClusterConfig(propertySupplier);
    }

    @Override
    protected void validate() throws Exception {
        this.tracer = Plugins.TRACERERVICE.get(PropertySupplier.getValue(propertySupplier, BrokerConfigKey.TRACER_TYPE));
        this.transportSessionManager = new TransportSessionManager(new TransportSessionConfig(propertySupplier),
                TransportConfigSupport.buildClientConfig(propertySupplier, ClusterConfigKey.TRANSPORT_KEY_PREFIX), new BrokerTransportClientFactory());
        this.clusterNodeManager = new ClusterNodeManager(nameService, eventBus);
        this.cache = new ClusterNameServiceCache(config, nameService);
        this.clusterNameServiceExecutorService = new ClusterNameServiceExecutorService(config);
    }

    @Override
    protected void doStart() throws Exception {
        this.cache.start();
        this.clusterNodeManager.start();
        this.transportSessionManager.start();
        this.clusterNameServiceExecutorService.start();
    }

    @Override
    protected void doStop() {
        this.clusterNameServiceExecutorService.stop();
        this.transportSessionManager.stop();
        this.clusterNodeManager.stop();
        this.cache.stop();
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
        this.clusterNodeManager.setBroker(broker);
    }

    public ClusterNode getTopicGroupNode(TopicName topicName, int group) {
        return getTopicGroupNode(topicName.getFullName(), group);
    }

    public ClusterNode getTopicGroupNode(String topic, int group) {
        return clusterNodeManager.getTopicGroupNode(topic, group);
    }

    public Map<TopicName, TopicConfig> getTopicConfigByApp(String subscribeApp, Subscription.Type subscribe) {
        Map<TopicName, TopicConfig> topicConfigMap = nameService.getTopicConfigByApp(subscribeApp, subscribe);
        if (MapUtils.isEmpty(topicConfigMap)) {
            return topicConfigMap;
        }
        if (topicConfigMap.size() > config.getTopicDynamicMetadataMaxBatchThreshold()) {
            return topicConfigMap;
        }

        TraceStat begin = tracer.begin("ClusterNameService.getTopicConfigByApp");
        try {
            CountDownLatch latch = new CountDownLatch(topicConfigMap.size());
            Map<TopicName, TopicConfig> result = Maps.newConcurrentMap();
            for (Map.Entry<TopicName, TopicConfig> entry : topicConfigMap.entrySet()) {
                if (config.getTopicDynamicMetadataBatchParallelEnable()) {
                    result.put(entry.getKey(), entry.getValue());
                    clusterNameServiceExecutorService.execute(() -> {
                        result.put(entry.getKey(), doGetTopicConfig(entry.getValue()));
                        latch.countDown();
                    });
                } else {
                    result.put(entry.getKey(), doGetTopicConfig(entry.getValue()));
                    latch.countDown();
                }
            }

            if (!latch.await(config.getTopicDynamicMetadataBatchTimeout(), TimeUnit.MILLISECONDS)) {
                logger.error("getTopicConfigByApp timeout, subscribeApp: {}, subscribe: {}", subscribeApp, subscribe);
            }
            tracer.end(begin);
            return result;
        } catch (Exception e) {
            logger.error("getTopicConfigByApp exception, subscribeApp: {}, subscribe: {}", subscribeApp, subscribe, e);
            tracer.error(begin);
            return topicConfigMap;
        }
    }

    public Map<String, TopicConfig> getTopicConfigs(List<String> topics) {
        Map<String, TopicConfig> topicConfigMap = Maps.newHashMap();
        for (String topic : topics) {
            TopicConfig topicConfig = nameService.getTopicConfig(TopicName.parse(topic));
            if (topicConfig != null) {
                topicConfigMap.put(topic, topicConfig);
            }
        }
        if (MapUtils.isEmpty(topicConfigMap)) {
            return topicConfigMap;
        }
        if (topicConfigMap.size() > config.getTopicDynamicMetadataMaxBatchThreshold()) {
            return topicConfigMap;
        }

        TraceStat begin = tracer.begin("ClusterNameService.getTopicConfigs");
        try {
            CountDownLatch latch = new CountDownLatch(topicConfigMap.size());
            Map<String, TopicConfig> result = Maps.newConcurrentMap();
            for (Map.Entry<String, TopicConfig> entry : topicConfigMap.entrySet()) {
                if (config.getTopicDynamicMetadataBatchParallelEnable()) {
                    result.put(entry.getKey(), entry.getValue());
                    clusterNameServiceExecutorService.execute(() -> {
                        result.put(entry.getKey(), doGetTopicConfig(entry.getValue()));
                        latch.countDown();
                    });
                } else {
                    result.put(entry.getKey(), doGetTopicConfig(entry.getValue()));
                    latch.countDown();
                }
            }

            if (!latch.await(config.getTopicDynamicMetadataBatchTimeout(), TimeUnit.MILLISECONDS)) {
                logger.error("getTopicConfigs timeout, topics: {}", topics);
            }

            tracer.end(begin);
            return result;
        } catch (Exception e) {
            logger.error("getTopicConfigs exception, topics: {}", topics, e);
            tracer.error(begin);
            return topicConfigMap;
        }
    }

    public TopicConfig getTopicConfig(TopicName topicName) {
        TopicConfig topicConfig = nameService.getTopicConfig(topicName);
        if (topicConfig == null) {
            return null;
        }
        return doGetTopicConfig(topicConfig);
    }

    public TopicConfig doGetTopicConfig(TopicConfig topicConfig) {
        if (!config.getTopicDynamicEnable()) {
            return topicConfig;
        }
        TopicConfig clone = ClusterSplitHelper.cloneTopicConfig(topicConfig);
        return getDynamicTopicConfig(clone);
    }

    protected TopicConfig getDynamicTopicConfig(TopicConfig topicConfig) {
        SplittedCluster splittedCluster = ClusterSplitHelper.split(topicConfig, clusterNodeManager);
        if (splittedCluster.isLocal()) {
            return topicConfig;
        }
        try {
            if (config.getTopicDynamicMetadataCacheEnable()) {
                try {
                    TopicConfig finalTopicConfig = topicConfig;
                    topicConfig = cache.getTopicConfig(topicConfig.getName(), () -> {
                        return doGetDynamicTopicConfig(finalTopicConfig, splittedCluster);
                    });
                } catch (Exception e) {
                    logger.error("get dynamic topic config exception, topic: {}", topicConfig.getName(), e);
                }
            } else {
                topicConfig = doGetDynamicTopicConfig(topicConfig, splittedCluster);
            }
            return topicConfig;
        } catch (Exception e) {
            logger.error("get dynamic topic config exception, topic: {}", topicConfig.getName(), e);
            return topicConfig;
        }
    }

    protected TopicConfig doGetDynamicTopicConfig(TopicConfig topicConfig, SplittedCluster splittedCluster) {
        TraceStat begin = tracer.begin("ClusterNameService.doGetDynamicTopicConfig");
        try {
            // 如果全部broker小于阈值，全量获取
            if (splittedCluster.getSplittedByGroup().size() < config.getTopicDynamicMetadataMinParallelThreshold()) {
                doGetRemoteTopicConfig(topicConfig, splittedCluster.getSplittedByGroup());
                return topicConfig;
            }

            // 尝试从leader获取
            if (!doGetRemoteTopicConfig(topicConfig, splittedCluster.getSplittedByLeader())) {
                Map<Integer, List<Integer>> splittedByRewrite = ClusterSplitHelper.splitByReWrite(topicConfig);
                for (Map.Entry<Integer, List<Integer>> entry : splittedCluster.getSplittedByLeader().entrySet()) {
                    splittedByRewrite.remove(entry.getKey());
                }
                if (splittedByRewrite.size() > config.getTopicDynamicMetadataMaxParallelThreshold()) {
                    // 记监控
                    tracer.end(tracer.begin("ClusterNameService.doGetDynamicTopicConfig.all." + topicConfig.getName().getFullName()));
                } else {
                    doGetRemoteTopicConfig(topicConfig, splittedByRewrite);
                }
            }
            tracer.end(begin);
            return topicConfig;
        } catch (Exception e) {
            tracer.error(begin);
            throw e;
        }
    }

    protected boolean doGetRemoteTopicConfig(TopicConfig topicConfig, Map<Integer /** brokerId **/, List<Integer /** group **/>> splittedByGroup) {
        if (MapUtils.isEmpty(splittedByGroup)) {
            return true;
        }
        TraceStat begin = tracer.begin("ClusterNameService.doGetRemoteTopicConfig");
        try {
            Map<Integer, Broker> brokerMap = topicConfig.fetchAllBroker();
            CountDownLatch latch = new CountDownLatch(splittedByGroup.size());
            Map<Integer, GetPartitionGroupClusterResponse.PartitionGroupCluster> partitionGroupClusterMap = Maps.newConcurrentMap();
            boolean[] isSuccess = {true};

            for (Map.Entry<Integer, List<Integer>> entry : splittedByGroup.entrySet()) {
                Broker broker = brokerMap.get(entry.getKey());
                if (broker == null) {
                    latch.countDown();
                    logger.error("broker not exist, topic: {}, broker: {}", topicConfig.getName(), entry.getKey());
                    continue;
                }
                TransportSession session = transportSessionManager.getOrCreateSession(broker);
                GetPartitionGroupClusterRequest getPartitionGroupClusterRequest = new GetPartitionGroupClusterRequest();
                Map<String, List<Integer>> groups = Maps.newHashMap();
                groups.put(topicConfig.getName().getFullName(), entry.getValue());
                getPartitionGroupClusterRequest.setGroups(groups);
                session.async(new JoyQueueCommand(getPartitionGroupClusterRequest), config.getTopicDynamicMetadataTransportTimeout(), new CommandCallback() {
                    @Override
                    public void onSuccess(Command request, Command response) {
                        GetPartitionGroupClusterResponse getPartitionGroupClusterResponse = (GetPartitionGroupClusterResponse) response.getPayload();
                        if (MapUtils.isNotEmpty(getPartitionGroupClusterResponse.getGroups())) {
                            for (Map.Entry<String, Map<Integer, GetPartitionGroupClusterResponse.PartitionGroupCluster>> entry : getPartitionGroupClusterResponse.getGroups().entrySet()) {
                                for (Map.Entry<Integer, GetPartitionGroupClusterResponse.PartitionGroupCluster> clusterEntry : entry.getValue().entrySet()) {
                                    partitionGroupClusterMap.putIfAbsent(clusterEntry.getKey(), clusterEntry.getValue());
                                }
                            }
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onException(Command request, Throwable cause) {
                        logger.error("get topic remote metadata exception, topic: {}, group: {}, broker: {}", topicConfig.getName(), entry.getValue(), entry.getKey(), cause);
                        isSuccess[0] = false;
                        latch.countDown();
                    }
                });
            }

            try {
                if (!latch.await(config.getTopicDynamicMetadataTimeout(), TimeUnit.MILLISECONDS)) {
                    isSuccess[0] = false;
                    logger.error("get topic remote metadata timeout, topic: {}, group: {}", topicConfig.getName(), splittedByGroup);
                }
            } catch (InterruptedException e) {
                logger.error("get topic remote metadata timeout, topic: {}, group: {}", topicConfig.getName(), splittedByGroup);
            }

            for (Map.Entry<Integer, GetPartitionGroupClusterResponse.PartitionGroupCluster> entry : partitionGroupClusterMap.entrySet()) {
                GetPartitionGroupClusterResponse.PartitionGroupCluster cluster = entry.getValue();
                GetPartitionGroupClusterResponse.PartitionGroupNode rwNode = cluster.getRWNode();
                if (rwNode == null) {
                    continue;
                }
                ClusterPartitionGroup partitionGroup = (ClusterPartitionGroup) topicConfig.getPartitionGroups().get(entry.getKey());
                if (partitionGroup != null) {
                    partitionGroup.setLeader(rwNode.getId());
                    partitionGroup.setRewrite(true);
                }
            }

            tracer.end(begin);
            return isSuccess[0];
        } catch (Exception e) {
            tracer.error(begin);
            throw e;
        }
    }

    public NameService getNameService() {
        return nameService;
    }
}