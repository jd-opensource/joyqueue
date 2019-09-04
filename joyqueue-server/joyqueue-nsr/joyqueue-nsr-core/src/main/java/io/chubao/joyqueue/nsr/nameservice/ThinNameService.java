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
package io.chubao.joyqueue.nsr.nameservice;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.jd.laf.extension.Type;
import io.chubao.joyqueue.domain.AllMetadata;
import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.ClientType;
import io.chubao.joyqueue.domain.Config;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.Subscription;
import io.chubao.joyqueue.domain.Topic;
import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.NameServerEvent;
import io.chubao.joyqueue.network.command.GetTopics;
import io.chubao.joyqueue.network.command.GetTopicsAck;
import io.chubao.joyqueue.network.command.SubscribeAck;
import io.chubao.joyqueue.network.command.UnSubscribe;
import io.chubao.joyqueue.network.event.TransportEvent;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.TransportClient;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.CommandCallback;
import io.chubao.joyqueue.network.transport.command.Direction;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.nsr.config.NameServiceConfig;
import io.chubao.joyqueue.nsr.network.NsrTransportClientFactory;
import io.chubao.joyqueue.nsr.network.command.AddTopic;
import io.chubao.joyqueue.nsr.network.command.GetAllBrokers;
import io.chubao.joyqueue.nsr.network.command.GetAllBrokersAck;
import io.chubao.joyqueue.nsr.network.command.GetAllConfigs;
import io.chubao.joyqueue.nsr.network.command.GetAllConfigsAck;
import io.chubao.joyqueue.nsr.network.command.GetAllMetadataRequest;
import io.chubao.joyqueue.nsr.network.command.GetAllMetadataResponse;
import io.chubao.joyqueue.nsr.network.command.GetAllTopics;
import io.chubao.joyqueue.nsr.network.command.GetAllTopicsAck;
import io.chubao.joyqueue.nsr.network.command.GetAppToken;
import io.chubao.joyqueue.nsr.network.command.GetAppTokenAck;
import io.chubao.joyqueue.nsr.network.command.GetBroker;
import io.chubao.joyqueue.nsr.network.command.GetBrokerAck;
import io.chubao.joyqueue.nsr.network.command.GetBrokerByRetryType;
import io.chubao.joyqueue.nsr.network.command.GetBrokerByRetryTypeAck;
import io.chubao.joyqueue.nsr.network.command.GetConfig;
import io.chubao.joyqueue.nsr.network.command.GetConfigAck;
import io.chubao.joyqueue.nsr.network.command.GetConsumerByTopic;
import io.chubao.joyqueue.nsr.network.command.GetConsumerByTopicAck;
import io.chubao.joyqueue.nsr.network.command.GetConsumerByTopicAndApp;
import io.chubao.joyqueue.nsr.network.command.GetConsumerByTopicAndAppAck;
import io.chubao.joyqueue.nsr.network.command.GetDataCenter;
import io.chubao.joyqueue.nsr.network.command.GetDataCenterAck;
import io.chubao.joyqueue.nsr.network.command.GetProducerByTopic;
import io.chubao.joyqueue.nsr.network.command.GetProducerByTopicAck;
import io.chubao.joyqueue.nsr.network.command.GetProducerByTopicAndApp;
import io.chubao.joyqueue.nsr.network.command.GetProducerByTopicAndAppAck;
import io.chubao.joyqueue.nsr.network.command.GetReplicaByBroker;
import io.chubao.joyqueue.nsr.network.command.GetReplicaByBrokerAck;
import io.chubao.joyqueue.nsr.network.command.GetTopicConfig;
import io.chubao.joyqueue.nsr.network.command.GetTopicConfigAck;
import io.chubao.joyqueue.nsr.network.command.GetTopicConfigByApp;
import io.chubao.joyqueue.nsr.network.command.GetTopicConfigByAppAck;
import io.chubao.joyqueue.nsr.network.command.GetTopicConfigByBroker;
import io.chubao.joyqueue.nsr.network.command.GetTopicConfigByBrokerAck;
import io.chubao.joyqueue.nsr.network.command.HasSubscribe;
import io.chubao.joyqueue.nsr.network.command.HasSubscribeAck;
import io.chubao.joyqueue.nsr.network.command.LeaderReport;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.chubao.joyqueue.nsr.network.command.NsrConnection;
import io.chubao.joyqueue.nsr.network.command.Register;
import io.chubao.joyqueue.nsr.network.command.RegisterAck;
import io.chubao.joyqueue.toolkit.concurrent.EventBus;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;
import io.chubao.joyqueue.toolkit.lang.Close;
import io.chubao.joyqueue.toolkit.lang.LifeCycle;
import io.chubao.joyqueue.toolkit.service.Service;
import io.chubao.joyqueue.toolkit.time.SystemClock;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class ThinNameService extends Service implements NameService, PropertySupplierAware, Type {
    private static final Logger logger = LoggerFactory.getLogger(ThinNameService.class);

    private NameServiceConfig nameServiceConfig;

    private ClientTransport clientTransport;
    private PropertySupplier propertySupplier;
    private Broker broker;
    /**
     * 事件管理器
     */
    protected EventBus<NameServerEvent> eventBus = new EventBus<>("BROKER_THIN_NAMESERVICE_ENENT_BUS");

    private Cache<TopicName, TopicConfig> topicCache;

    public ThinNameService() {
        //do nothing
    }

    public ThinNameService(NameServiceConfig nameServiceConfig) {
        this.nameServiceConfig = nameServiceConfig;
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        if (nameServiceConfig == null) {
            nameServiceConfig = new NameServiceConfig(propertySupplier);
        }
        if (topicCache == null) {
            topicCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(nameServiceConfig.getThinCacheExpireTime(), TimeUnit.MILLISECONDS)
                    .build();
        }
        clientTransport = new ClientTransport(nameServiceConfig,this);
    }

    @Override
    public TopicConfig subscribe(Subscription subscription, ClientType clientType) {
        List<TopicConfig> topicConfigs = subscribe(Lists.newArrayList(subscription), clientType);
        if (CollectionUtils.isEmpty(topicConfigs)) {
            return null;
        }
        return topicConfigs.get(0);
    }

    @Override
    public List<TopicConfig> subscribe(List<Subscription> subscriptions, ClientType clientType) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.SUBSCRIBE),
                new io.chubao.joyqueue.network.command.Subscribe().subscriptions(subscriptions).clientType(clientType));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("subscribe error request {},response {}", request, response);
            throw new RuntimeException(String.format("subscribe error request {},response {}", request, response));
        }
        return ((SubscribeAck) response.getPayload()).getTopicConfigs();
    }

    @Override
    public void unSubscribe(Subscription subscription) {
        unSubscribe(Lists.newArrayList(subscription));
    }

    @Override
    public void unSubscribe(List<Subscription> subscriptions) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.UN_SUBSCRIBE), new UnSubscribe().subscriptions(subscriptions));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("unSubscribe error request {},response {}", request, response);
            throw new RuntimeException(String.format("unSubscribe error request {},response {}", request, response));
        }
    }

    @Override
    public boolean hasSubscribe(String app, Subscription.Type subscribe) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.HAS_SUBSCRIBE), new HasSubscribe().app(app).subscribe(subscribe));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("hasSubscribe error request {},response {}", request, response);
            throw new RuntimeException(String.format("hasSubscribe error request {},response {}", request, response));
        }
        return ((HasSubscribeAck) response.getPayload()).isHave();
    }

    @Override
    public void leaderReport(TopicName topic, int partitionGroup, int leaderBrokerId, Set<Integer> isrId, int termId) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.LEADER_REPORT),
                new LeaderReport().topic(topic).partitionGroup(partitionGroup).leaderBrokerId(leaderBrokerId).isrId(isrId).termId(termId));
        sendAsync(request, new CommandCallback() {
            @Override
            public void onSuccess(Command request, Command response) {
                logger.info("Report leader of topic {} partition group {} to nameserver success",
                        topic, partitionGroup);
            }

            @Override
            public void onException(Command request, Throwable cause) {
                logger.info("Report leader of topic {} partition group {} to nameserver fail",
                        topic, partitionGroup, cause);
            }
        });
    }

    @Override
    public Broker getBroker(int brokerId) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_BROKER), new GetBroker().brokerId(brokerId));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getBroker error request {},response {}", request, response);
            throw new RuntimeException(String.format("getBroker error request {},response {}", request, response));
        }
        return ((GetBrokerAck) response.getPayload()).getBroker();
    }

    @Override
    public List<Broker> getAllBrokers() {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_ALL_BROKERS), new GetAllBrokers());
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getAllBrokers error request {},response {}", request, response);
            throw new RuntimeException(String.format("getAllBrokers error request {},response {}", request, response));
        }
        return ((GetAllBrokersAck) response.getPayload()).getBrokers();
    }

    @Override
    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.ADD_TOPIC), new AddTopic().topic(topic).partitiionGroups(partitionGroups));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("leaderReport error request {},response {}", request, response);
            throw new RuntimeException(String.format("leaderReport error request {},response {}", request, response));
        }
    }

    @Override
    public TopicConfig getTopicConfig(TopicName topic) {
        if (nameServiceConfig.getThinCacheEnable()) {
            try {
                return topicCache.get(topic, new Callable<TopicConfig>() {
                    @Override
                    public TopicConfig call() throws Exception {
                        return doGetTopicConfig(topic);
                    }
                });
            } catch (ExecutionException e) {
                logger.error("getTopicConfig exception, topic: {}", topic, e);
                return null;
            }
        } else {
            return doGetTopicConfig(topic);
        }
    }

    protected TopicConfig doGetTopicConfig(TopicName topic) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_TOPICCONFIG), new GetTopicConfig().topic(topic));
        Command response = send(request, nameServiceConfig.getThinTransportTopicTimeout());
        if (!response.isSuccess()) {
            logger.error("getTopicConfig error request {},response {}", request, response);
            throw new RuntimeException(String.format("getTopicConfig error request {},response {}", request, response));
        }
        TopicConfig topicConfig = ((GetTopicConfigAck) response.getPayload()).getTopicConfig();
        return topicConfig;
    }

    @Override
    public Set<String> getAllTopicCodes() {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_ALL_TOPICS), new GetAllTopics());
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getAllTopicCodes error request {},response {}", request, response);
            throw new RuntimeException(String.format("getAllTopicCodes error request {},response {}", request, response));
        }
        return ((GetAllTopicsAck) response.getPayload()).getTopicNames();
    }

    @Override
    public Set<String> getTopics(String app, Subscription.Type subscription) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_TOPICS), new GetTopics().app(app).subscribeType(subscription.getValue()));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getTopics error request {},response {}", request, response);
            throw new RuntimeException(String.format("getTopics error request {},response {}", request, response));
        }
        return ((GetTopicsAck) response.getPayload()).getTopics();
    }

    @Override
    public Map<TopicName, TopicConfig> getTopicConfigByBroker(Integer brokerId) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_TOPICCONFIGS_BY_BROKER), new GetTopicConfigByBroker().brokerId(brokerId));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getTopicConfigByBroker error request {},response {}", request, response);
            throw new RuntimeException(String.format("getTopicConfigByBroker error request {},response {}", request, response));
        }
        return ((GetTopicConfigByBrokerAck) response.getPayload()).getTopicConfigs();
    }

    @Override
    public Broker register(Integer brokerId, String brokerIp, Integer port) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.REGISTER), new Register().brokerId(brokerId).brokerIp(brokerIp).port(port));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("register error request {},response {}", request, response);
            throw new RuntimeException(String.format("getTopicConfigByBroker error request {},response {}", request, response));
        }
        broker = ((RegisterAck) response.getPayload()).getBroker();
        return broker;
    }

    @Override
    public Producer getProducerByTopicAndApp(TopicName topic, String app) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_PRODUCER_BY_TOPIC_AND_APP), new GetProducerByTopicAndApp().topic(topic).app(app));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getProducerByTopicAndApp error request {},response {}", request, response);
            throw new RuntimeException(String.format("getProducerByTopicAndApp error request {},response {}", request, response));
        }
        return ((GetProducerByTopicAndAppAck) response.getPayload()).getProducer();
    }

    @Override
    public Consumer getConsumerByTopicAndApp(TopicName topic, String app) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_CONSUMER_BY_TOPIC_AND_APP), new GetConsumerByTopicAndApp().topic(topic).app(app));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getConsumerByTopicAndApp error request {},response {}", request, response);
            throw new RuntimeException(String.format("getConsumerByTopicAndApp error request {},response {}", request, response));
        }
        return ((GetConsumerByTopicAndAppAck) response.getPayload()).getConsumer();
    }

    @Override
    public Map<TopicName, TopicConfig> getTopicConfigByApp(String app, Subscription.Type subscribe) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_TOPICCONFIGS_BY_APP), new GetTopicConfigByApp().subscribe(subscribe).app(app));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getTopicConfigByApp error request {},response {}", request, response);
            throw new RuntimeException(String.format("getTopicConfigByApp error request {},response {}", request, response));
        }
        return ((GetTopicConfigByAppAck) response.getPayload()).getTopicConfigs();
    }

    @Override
    public DataCenter getDataCenter(String ip) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_DATACENTER), new GetDataCenter().ip(ip));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getTopicConfigByApp error request {},response {}", request, response);
            throw new RuntimeException(String.format("getTopicConfigByApp error request {},response {}", request, response));
        }
        return ((GetDataCenterAck) response.getPayload()).getDataCenter();
    }

    @Override
    public String getConfig(String group, String key) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_CONFIG), new GetConfig().group(group).key(key));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getConfig error request {},response {}", request, response);
            throw new RuntimeException(String.format("getConfig error request {},response {}", request, response));
        }
        return ((GetConfigAck) response.getPayload()).getValue();
    }

    @Override
    public List<Config> getAllConfigs() {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_ALL_CONFIG), new GetAllConfigs());
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getAllConfigs error request {},response {}", request, response);
            throw new RuntimeException(String.format("getAllConfigs error request {},response {}", request, response));
        }
        return ((GetAllConfigsAck) response.getPayload()).getConfigs();
    }

    @Override
    public List<Broker> getBrokerByRetryType(String retryType) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_BROKER_BY_RETRYTYPE), new GetBrokerByRetryType().retryType(retryType));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getBrokerByRetryType error request {},response {}", request, response);
            throw new RuntimeException(String.format("getBrokerByRetryType error request {},response {}", request, response));
        }
        return ((GetBrokerByRetryTypeAck) response.getPayload()).getBrokers();
    }

    @Override
    public List<Consumer> getConsumerByTopic(TopicName topic) {
        List<Consumer> consumers = new ArrayList<>();
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_CONSUMER_BY_TOPIC), new GetConsumerByTopic().topic(topic));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getConsumerByTopic error request {},response {}", request, response);
            throw new RuntimeException(String.format("getConsumerByTopic error request {},response {}", request, response));
        }
        List<Consumer> topicConsumes = ((GetConsumerByTopicAck) response.getPayload()).getConsumers();
        if (null != topicConsumes) {
            consumers.addAll(topicConsumes);
        }
        return consumers;
    }

    @Override
    public List<Producer> getProducerByTopic(TopicName topic) {
        List<Producer> producers = new ArrayList<>();
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_PRODUCER_BY_TOPIC), new GetProducerByTopic().topic(topic));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getProducerByTopic error request {},response {}", request, response);
            throw new RuntimeException(String.format("getProducerByTopic error request {},response {}", request, response));
        }
        List<Producer> topicProducers = ((GetProducerByTopicAck) response.getPayload()).getProducers();
        if (null != topicProducers) {
            producers.addAll(topicProducers);
        }
        return producers;
    }

    @Override
    public List<Replica> getReplicaByBroker(Integer brokerId) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_REPLICA_BY_BROKER), new GetReplicaByBroker().brokerId(brokerId));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getReplicaByBroker error request {},response {}", request, response);
            throw new RuntimeException(String.format("getReplicaByBroker error request {},response {}", request, response));
        }
        return ((GetReplicaByBrokerAck) response.getPayload()).getReplicas();
    }

    @Override
    public AppToken getAppToken(String app, String token) {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.GET_APP_TOKEN), new GetAppToken().app(app).token(token));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getAppToken error request {},response {}", request, response);
            throw new RuntimeException(String.format("getAppToken error request {},response {}", request, response));
        }
        return ((GetAppTokenAck) response.getPayload()).getAppToken();
    }

    @Override
    public AllMetadata getAllMetadata() {
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.NSR_GET_ALL_METADATA_REQUEST), new GetAllMetadataRequest());
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getAllMetadata error request {},response {}", request, response);
            throw new RuntimeException(String.format("getAppToken error request {},response {}", request, response));
        }
        return ((GetAllMetadataResponse) response.getPayload()).getMetadata();
    }

    @Override
    public void addListener(EventListener<NameServerEvent> listener) {
        eventBus.addListener(listener);
    }

    @Override
    public void removeListener(EventListener<NameServerEvent> listener) {
        eventBus.removeListener(listener);
    }

    @Override
    public void addEvent(NameServerEvent event) {
        eventBus.add(event);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        eventBus.start();
        clientTransport.start();
    }

    @Override
    protected void doStop() {
        super.doStop();
        Close.close(clientTransport);
        Close.close(eventBus);
        logger.info("name service stopped.");
    }

    private Command send(Command request) throws TransportException {
        return send(request, nameServiceConfig.getThinTransportTimeout());
    }

    private Command send(Command request, int timeout) throws TransportException {
        // TODO 临时监控
        long startTime = SystemClock.now();
        try {
            return clientTransport.getOrCreateTransport().sync(request, timeout);
        } catch (TransportException exception) {
            logger.error("send command to nameServer error request {}", request);
            throw exception;
        } finally {
            long time = SystemClock.now() - startTime;
            if (time > 1000 * 1) {
                logger.warn("thinNameService timeout, request: {}, time: {}", request, time);
            }
        }
    }

    private void sendAsync(Command request, CommandCallback callback) throws TransportException {
        sendAsync(request, nameServiceConfig.getThinTransportTimeout(), callback);
    }

    private void sendAsync(Command request, int timeout, CommandCallback callback) throws TransportException {
        try {
            clientTransport.getOrCreateTransport().async(request, timeout, callback);
        } catch (TransportException exception) {
            logger.error("send command to nameServer error request {}", request);
            throw exception;
        }
    }

    private Command registerToNsr() throws TransportException {
        if (null == broker) {
            return null;
        }
        Command request = new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.CONNECT), new NsrConnection().brokerId(broker.getId()));
        return send(request);
    }

    @Override
    public Object type() {
        return "thin";
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.propertySupplier = supplier;
    }


    private class ClientTransport implements EventListener<TransportEvent>, LifeCycle {
        private AtomicBoolean started = new AtomicBoolean(false);
        private TransportClient transportClient;
        protected final AtomicReference<Transport> transports = new AtomicReference<>();

        ClientTransport(NameServiceConfig config, NameService nameService) {
            this.transportClient = new NsrTransportClientFactory(nameService, propertySupplier).create(config.getClientConfig());
            this.transportClient.addListener(this);
        }


        @Override
        public void onEvent(TransportEvent event) {
            Transport transport = event.getTransport();
            switch (event.getType()) {
                case CONNECT:
                    registerToNsr();
                    break;
                case EXCEPTION:
                case CLOSE:
                    transports.set(null);
                    transport.stop();
                    logger.info("transport connect to nameServer closed. [{}] ", transport.toString());
                    break;
                default:
                    break;
            }
        }

        protected Transport getOrCreateTransport() throws TransportException {
            Transport transport = transports.get();
            if (transport == null) {
                synchronized (this) {
                    transport = transports.get();
                    if (transport == null) {
                        transport = transportClient.createTransport(nameServiceConfig.getNameserverAddress());
                        transports.set(transport);
                    }
                    logger.info("create transport connect to nameServer [{}]", nameServiceConfig.getNameserverAddress());
                }
            }
            return transport;
        }


        @Override
        public void start() throws Exception {
            if (started.compareAndSet(false, true)) {
                this.transportClient.start();
            }
        }

        @Override
        public void stop() {
            started.set(false);
            this.transportClient.stop();
        }

        @Override
        public boolean isStarted() {
            return started.get();
        }
    }


}
