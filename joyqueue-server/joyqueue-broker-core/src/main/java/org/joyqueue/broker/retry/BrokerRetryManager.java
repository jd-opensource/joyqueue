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
package org.joyqueue.broker.retry;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jd.laf.extension.ExtensionManager;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.Plugins;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.limit.RateLimiter;
import org.joyqueue.broker.limit.SubscribeRateLimiter;
import org.joyqueue.broker.monitor.BrokerMonitor;
import org.joyqueue.broker.network.support.BrokerTransportClientFactory;
import org.joyqueue.config.BrokerConfigKey;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.Subscription;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.EventType;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.monitor.PointTracer;
import org.joyqueue.monitor.TraceStat;
import org.joyqueue.network.session.Joint;
import org.joyqueue.network.transport.TransportClient;
import org.joyqueue.network.transport.config.ClientConfig;
import org.joyqueue.network.transport.config.TransportConfigSupport;
import org.joyqueue.nsr.NameService;
import org.joyqueue.nsr.event.UpdateBrokerEvent;
import org.joyqueue.server.retry.NullMessageRetry;
import org.joyqueue.server.retry.api.MessageRetry;
import org.joyqueue.server.retry.api.RetryPolicyProvider;
import org.joyqueue.server.retry.model.RetryMessageModel;
import org.joyqueue.server.retry.remote.RemoteMessageRetry;
import org.joyqueue.server.retry.remote.RemoteRetryProvider;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.config.Property;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.retry.RetryPolicy;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.joyqueue.domain.Broker.DEFAULT_RETRY_TYPE;

/**
 * 服务端重试消息管理
 */
public class BrokerRetryManager extends Service implements MessageRetry<Long>, BrokerContextAware {
    private static final Logger logger = LoggerFactory.getLogger(BrokerRetryManager.class);
    // 重试消息服务
    private MessageRetry delegate;
    // 事件监听
    private EventListener eventListener = new BrokerRetryEventListener();
    // 重试类型
    private volatile String retryType;
    // 重试策略
    private RetryPolicyProvider retryPolicyProvider;
    // 远程重试帮助
    private RemoteRetryProvider remoteRetryProvider;
    // 注册中心
    private NameService nameService;
    // 集群管理
    private ClusterManager clusterManager;
    private PropertySupplier propertySupplier;
    private SubscribeRateLimiter rateLimiterManager;
    private BrokerMonitor brokerMonitor;
    private PointTracer tracer;

    public BrokerRetryManager(BrokerContext brokerContext) {
        setBrokerContext(brokerContext);
        this.rateLimiterManager=new BrokerRetryRateLimiterManager(brokerContext);
    }

    @Override
    protected void validate() throws Exception {
        super.validate();

        if (retryPolicyProvider == null) {
            retryPolicyProvider =  new RetryPolicyProviderImpl();
        }

        if (remoteRetryProvider == null) {
            remoteRetryProvider = new RemoteRetryProvider() {
                @Override
                public Set<String> getUrls() {
                    List<Broker> brokers = clusterManager.getLocalRetryBroker();

                    logger.info("broker list:{}", Arrays.toString(brokers.toArray()));

                    Set<String /*url=ip:port*/> urlSet = new HashSet<>();
                    for (Broker broker : brokers) {
                        urlSet.add(broker.getIp() + ":" + broker.getBackEndPort());
                    }

                    return urlSet;
                }

                @Override
                public TransportClient createTransportClient() {
                    ClientConfig clientConfig = TransportConfigSupport.buildClientConfig(propertySupplier, "retry.remote.client");
                    clientConfig.setIoThreadName("joyqueue-retry-io-eventLoop");
                    return new BrokerTransportClientFactory().create(clientConfig);
                }
            };
        }
    }

    class RetryPolicyProviderImpl implements RetryPolicyProvider {

        // 通过CacheBuilder构建一个缓存实例
        private final Cache<String, RetryPolicy> cache = CacheBuilder.newBuilder()
                .maximumSize(1000000) // 设置缓存的最大容量
                .expireAfterWrite(1, TimeUnit.MINUTES) // 设置缓存在写入一分钟后失效
                .concurrencyLevel(Runtime.getRuntime().availableProcessors()) // 设置并发级别为10
                .recordStats() // 开启缓存统计
                .build();

        @Override
        public RetryPolicy getPolicy(TopicName topic, String app) {
            String cacheKey = getKey(topic, app);
            // 尝试从缓存获取
            RetryPolicy retryPolicy = cache.getIfPresent(cacheKey);
            if (retryPolicy == null) {
                Consumer consumerByTopicAndApp = nameService.getConsumerByTopicAndApp(topic, app);
                if (consumerByTopicAndApp == null) {
                    logger.debug("nameService.getConsumerByTopicAndApp is null by topic:[{}], app:[{}]", topic, app);

                    retryPolicy = new RetryPolicy();
                    cache.put(cacheKey, retryPolicy);
                    return retryPolicy;
                }

                retryPolicy = consumerByTopicAndApp.getRetryPolicy();
                if (retryPolicy == null) {
                    logger.debug("consumerByTopicAndApp.getRetryPolicy() is null by topic:[{}], app:[{}]", topic, app);

                    retryPolicy = new RetryPolicy();
                    cache.put(cacheKey, retryPolicy);
                    return retryPolicy;
                } else {
                    logger.debug("Get RetryPolicy:[{}] by topic:[{}], app:[{}], ", retryPolicy.toString(), topic.getFullName(), app);
                }

                cache.put(cacheKey, retryPolicy);
                return retryPolicy;
            }
            return retryPolicy;
        }

        private String getKey(TopicName topic, String app) {
            return topic.getFullName() + ":" + app;
        }

    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        clusterManager.addListener(eventListener);
        clusterManager.addListener(rateLimiterManager);
        retryType = clusterManager.getBroker().getRetryType();
        delegate = loadRetryManager(retryType);
    }

    @Override
    public void setRetryPolicyProvider(RetryPolicyProvider retryPolicyProvider) {
        this.retryPolicyProvider = retryPolicyProvider;
    }


    @Override
    public void addRetry(List<RetryMessageModel> retryMessageModelList) throws JoyQueueException {
        if (CollectionUtils.isEmpty(retryMessageModelList)) {
            return;
        }
        String topic = retryMessageModelList.get(0).getTopic();
        String app = retryMessageModelList.get(0).getApp();
        Consumer consumer = clusterManager.getNameService().getConsumerByTopicAndApp(TopicName.parse(topic), app);
        if (consumer != null && consumer.getConsumerPolicy() != null && consumer.getConsumerPolicy().getRetry() != null && !consumer.getConsumerPolicy().getRetry()) {
            throw new JoyQueueException(JoyQueueCode.RETRY_TOKEN_LIMIT);
        }

        Set<Joint> consumers= retryConsumers(retryMessageModelList);
        if(retryTokenAvailable(consumers)) {
            TraceStat totalRetryTrace = tracer.begin("BrokerRetryManager.addRetry");
            TraceStat appRetryTrace = tracer.begin(String.format("BrokerRetryManager.addRetry.%s.%s", app.replace(".", "_"), topic));
            try {
                long startTime = SystemClock.now();
                delegate.addRetry(retryMessageModelList);
                brokerMonitor.onAddRetry(topic, app, retryMessageModelList.size(), SystemClock.now() - startTime);
                tracer.end(totalRetryTrace);
                tracer.end(appRetryTrace);
            } catch (Exception e) {
                tracer.error(totalRetryTrace);
                tracer.error(appRetryTrace);
                throw e;
            }
        }else{
            TraceStat limit = tracer.begin("BrokerRetryManager.rate.limited");
            logger.warn("Broker retry message limited, limit consumers: {}", consumers);
            tracer.end(limit);
            throw new JoyQueueException(JoyQueueCode.RETRY_TOKEN_LIMIT);
        }
    }


    /**
     *
     * @return true if any of topic has retry token or ulimit
     *
     **/
    public boolean retryTokenAvailable(Set<Joint> consumers){
        for(Joint consumer:consumers) {
            RateLimiter rateLimiter= rateLimiterManager.getOrCreate(consumer.getTopic(),consumer.getApp(), Subscription.Type.CONSUMPTION);
            if(rateLimiter==null||rateLimiter.tryAcquireTps()){
                return true;
            }
        }
        return false;
    }

    /**
     * @return  retry consumers
     *
     **/
    public Set<Joint> retryConsumers(List<RetryMessageModel> retryMessageModelList){
        Set<Joint> consumers=new HashSet(retryMessageModelList.size());
        for(RetryMessageModel m:retryMessageModelList){
            consumers.add(new Joint(m.getTopic(),m.getApp()));
        }
        return consumers;
    }

    @Override
    public void retrySuccess(String topic, String app, Long[] messageIds) throws JoyQueueException {
        if (messageIds == null) {
            return;
        }
        delegate.retrySuccess(topic, app, messageIds);
        brokerMonitor.onRetrySuccess(topic, app, messageIds.length);
    }

    @Override
    public void retryError(String topic, String app, Long[] messageIds) throws JoyQueueException {
        if (messageIds == null) {
            return;
        }
        delegate.retryError(topic, app, messageIds);
        brokerMonitor.onRetryFailure(topic, app, messageIds.length);
    }

    @Override
    public void retryExpire(String topic, String app, Long[] messageIds) throws JoyQueueException {
        delegate.retryExpire(topic, app, messageIds);
    }

    @Override
    public List<RetryMessageModel> getRetry(String topic, String app, short count, long startId) throws JoyQueueException {
        return delegate.getRetry(topic, app, count, startId);
    }

    @Override
    public int countRetry(String topic, String app) throws JoyQueueException {
        return delegate.countRetry(topic, app);
    }

    private MessageRetry loadRetryManager(String type) throws Exception {
        MessageRetry messageRetry;

        Property retryEnabledProperty = propertySupplier.getProperty("retry.enable");
        boolean retryEnable = null == retryEnabledProperty ? false : retryEnabledProperty.getBoolean(false);

        if (!retryEnable) {
            messageRetry = new NullMessageRetry();
        } else if (type.equals(DEFAULT_RETRY_TYPE)) {
            messageRetry = new RemoteMessageRetry(remoteRetryProvider);
        } else {
            messageRetry = ExtensionManager.getOrLoadExtension(MessageRetry.class, type);
        }

        if (messageRetry == null) {
            throw new RuntimeException("No such implementation found." + type);
        }

        messageRetry.setSupplier(propertySupplier);
        messageRetry.setRetryPolicyProvider(retryPolicyProvider);

        messageRetry.start();

        return messageRetry;
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.nameService = brokerContext.getNameService();
        this.clusterManager = brokerContext.getClusterManager();
        this.propertySupplier = brokerContext.getPropertySupplier();
        this.brokerMonitor = brokerContext.getBrokerMonitor();
        this.tracer = Plugins.TRACERERVICE.get(PropertySupplier.getValue(propertySupplier, BrokerConfigKey.TRACER_TYPE));
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.propertySupplier = supplier;
    }

    /**
     * 监听broker的重试策略变化
     */
    protected class BrokerRetryEventListener implements EventListener<MetaEvent> {

        @Override
        public void onEvent(MetaEvent event) {
            try {
                if (event.getEventType() == EventType.UPDATE_BROKER) {
                    logger.info("listen update broker event.");
                    UpdateBrokerEvent updateBrokerEvent = (UpdateBrokerEvent) event;
                    Broker broker = updateBrokerEvent.getNewBroker();
                    String type = broker != null ? broker.getRetryType() : null;
                    if (type != null && !type.equals(retryType)) {
                        MessageRetry messageRetry = loadRetryManager(type);
                        if (messageRetry != null) {
                            MessageRetry pre = BrokerRetryManager.this.delegate;
                            if (pre != null) {
                                pre.stop();
                            }
                            BrokerRetryManager.this.delegate = messageRetry;
                        }
                    }

                    retryType = type; // 完成实现变更，将类型变更过来

                    logger.info("Broker Retry Mode is : {}", retryType);
                }
            } catch (Exception e) {
                logger.error("process broker retry event error.", e);
            }
        }

    }


}
