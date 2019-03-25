package com.jd.journalq.broker.retry;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.network.support.BrokerTransportClientFactory;
import com.jd.journalq.common.domain.Broker;
import com.jd.journalq.common.domain.Consumer;
import com.jd.journalq.common.event.BrokerEvent;
import com.jd.journalq.common.event.EventType;
import com.jd.journalq.common.event.MetaEvent;
import com.jd.journalq.common.exception.JMQException;
import com.jd.journalq.common.network.transport.TransportClient;
import com.jd.journalq.common.network.transport.config.ClientConfig;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.server.retry.api.MessageRetry;
import com.jd.journalq.server.retry.api.RetryPolicyProvider;
import com.jd.journalq.server.retry.model.RetryMessageModel;
import com.jd.journalq.server.retry.remote.RemoteMessageRetry;
import com.jd.journalq.server.retry.remote.RemoteRetryProvider;
import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.toolkit.service.Service;
import com.jd.laf.extension.ExtensionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.jd.journalq.common.domain.Broker.DEFAULT_RETRY_TYPE;

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
    //broker context
    private BrokerContext brokerContext;


    public BrokerRetryManager(BrokerContext brokerContext) {
        this.nameService = brokerContext.getNameService();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    protected void validate() throws Exception {
        super.validate();

        if (retryPolicyProvider == null) {
            retryPolicyProvider = (topic, app) -> {
                Consumer consumerByTopicAndApp = nameService.getConsumerByTopicAndApp(topic, app);
                return consumerByTopicAndApp.getRetryPolicy();
            };
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
                    return new BrokerTransportClientFactory().create(new ClientConfig());
                }
            };
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
    public void addRetry(List<RetryMessageModel> retryMessageModelList) throws JMQException {
        delegate.addRetry(retryMessageModelList);
    }

    @Override
    public void retrySuccess(String topic, String app, Long[] messageIds) throws JMQException {
        delegate.retrySuccess(topic, app, messageIds);
    }

    @Override
    public void retryError(String topic, String app, Long[] messageIds) throws JMQException {
        delegate.retryError(topic, app, messageIds);
    }

    @Override
    public void retryExpire(String topic, String app, Long[] messageIds) throws JMQException {
        delegate.retryExpire(topic, app, messageIds);
    }

    @Override
    public List<RetryMessageModel> getRetry(String topic, String app, short count, long startId) throws JMQException {
        return delegate.getRetry(topic, app, count, startId);
    }

    @Override
    public int countRetry(String topic, String app) throws JMQException {
        return delegate.countRetry(topic, app);
    }

    private MessageRetry loadRetryManager(String type) throws Exception {
        MessageRetry messageRetry;

        if (type.equals(DEFAULT_RETRY_TYPE)) {
            messageRetry = new RemoteMessageRetry(remoteRetryProvider);
        } else {
            messageRetry = ExtensionManager.getOrLoadExtension(MessageRetry.class, type);
        }

        if (messageRetry == null) {
            throw new RuntimeException("No such implementation found." + type);
        }

        messageRetry.setRetryPolicyProvider(retryPolicyProvider);

        messageRetry.start();

        return messageRetry;
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
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
                    Broker broker = ((BrokerEvent) event).getBroker();
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
