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
package io.chubao.joyqueue.broker.retry;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jd.laf.extension.ExtensionManager;
import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.network.support.BrokerTransportClientFactory;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.EventType;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.network.transport.TransportClient;
import io.chubao.joyqueue.network.transport.config.ClientConfig;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.nsr.event.UpdateBrokerEvent;
import io.chubao.joyqueue.server.retry.NullMessageRetry;
import io.chubao.joyqueue.server.retry.api.MessageRetry;
import io.chubao.joyqueue.server.retry.api.RetryPolicyProvider;
import io.chubao.joyqueue.server.retry.model.RetryMessageModel;
import io.chubao.joyqueue.server.retry.remote.RemoteMessageRetry;
import io.chubao.joyqueue.server.retry.remote.RemoteRetryProvider;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import io.chubao.joyqueue.toolkit.config.Property;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.retry.RetryPolicy;
import io.chubao.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.chubao.joyqueue.domain.Broker.DEFAULT_RETRY_TYPE;

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


    public BrokerRetryManager(BrokerContext brokerContext) {
        setBrokerContext(brokerContext);

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
                    ClientConfig clientConfig = new ClientConfig();
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

        retryType = clusterManager.getBroker().getRetryType();
        delegate = loadRetryManager(retryType);
    }

    @Override
    public void setRetryPolicyProvider(RetryPolicyProvider retryPolicyProvider) {
        this.retryPolicyProvider = retryPolicyProvider;
    }


    @Override
    public void addRetry(List<RetryMessageModel> retryMessageModelList) throws JoyQueueException {
        delegate.addRetry(retryMessageModelList);
    }

    @Override
    public void retrySuccess(String topic, String app, Long[] messageIds) throws JoyQueueException {
        delegate.retrySuccess(topic, app, messageIds);
    }

    @Override
    public void retryError(String topic, String app, Long[] messageIds) throws JoyQueueException {
        delegate.retryError(topic, app, messageIds);
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
