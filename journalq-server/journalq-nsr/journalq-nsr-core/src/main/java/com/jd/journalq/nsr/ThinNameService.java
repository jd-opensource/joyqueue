/**
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
package com.jd.journalq.nsr;

import com.google.common.collect.Lists;
import com.jd.journalq.domain.AppToken;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.ClientType;
import com.jd.journalq.domain.Config;
import com.jd.journalq.domain.Consumer;
import com.jd.journalq.domain.DataCenter;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.Producer;
import com.jd.journalq.domain.Replica;
import com.jd.journalq.domain.Subscription;
import com.jd.journalq.domain.Topic;
import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.event.NameServerEvent;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.command.GetTopics;
import com.jd.journalq.network.command.GetTopicsAck;
import com.jd.journalq.network.command.SubscribeAck;
import com.jd.journalq.network.command.UnSubscribe;
import com.jd.journalq.network.event.TransportEvent;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.TransportClient;
import com.jd.journalq.network.transport.codec.JournalqHeader;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.CommandCallback;
import com.jd.journalq.network.transport.command.Direction;
import com.jd.journalq.network.transport.exception.TransportException;
import com.jd.journalq.nsr.config.NameServiceConfig;
import com.jd.journalq.nsr.network.NsrTransportClientFactory;
import com.jd.journalq.nsr.network.command.AddTopic;
import com.jd.journalq.nsr.network.command.GetAllBrokers;
import com.jd.journalq.nsr.network.command.GetAllBrokersAck;
import com.jd.journalq.nsr.network.command.GetAllConfigs;
import com.jd.journalq.nsr.network.command.GetAllConfigsAck;
import com.jd.journalq.nsr.network.command.GetAllTopics;
import com.jd.journalq.nsr.network.command.GetAllTopicsAck;
import com.jd.journalq.nsr.network.command.GetAppToken;
import com.jd.journalq.nsr.network.command.GetAppTokenAck;
import com.jd.journalq.nsr.network.command.GetBroker;
import com.jd.journalq.nsr.network.command.GetBrokerAck;
import com.jd.journalq.nsr.network.command.GetBrokerByRetryType;
import com.jd.journalq.nsr.network.command.GetBrokerByRetryTypeAck;
import com.jd.journalq.nsr.network.command.GetConfig;
import com.jd.journalq.nsr.network.command.GetConfigAck;
import com.jd.journalq.nsr.network.command.GetConsumerByTopic;
import com.jd.journalq.nsr.network.command.GetConsumerByTopicAck;
import com.jd.journalq.nsr.network.command.GetConsumerByTopicAndApp;
import com.jd.journalq.nsr.network.command.GetConsumerByTopicAndAppAck;
import com.jd.journalq.nsr.network.command.GetDataCenter;
import com.jd.journalq.nsr.network.command.GetDataCenterAck;
import com.jd.journalq.nsr.network.command.GetProducerByTopic;
import com.jd.journalq.nsr.network.command.GetProducerByTopicAck;
import com.jd.journalq.nsr.network.command.GetProducerByTopicAndApp;
import com.jd.journalq.nsr.network.command.GetProducerByTopicAndAppAck;
import com.jd.journalq.nsr.network.command.GetReplicaByBroker;
import com.jd.journalq.nsr.network.command.GetReplicaByBrokerAck;
import com.jd.journalq.nsr.network.command.GetTopicConfig;
import com.jd.journalq.nsr.network.command.GetTopicConfigAck;
import com.jd.journalq.nsr.network.command.GetTopicConfigByApp;
import com.jd.journalq.nsr.network.command.GetTopicConfigByAppAck;
import com.jd.journalq.nsr.network.command.GetTopicConfigByBroker;
import com.jd.journalq.nsr.network.command.GetTopicConfigByBrokerAck;
import com.jd.journalq.nsr.network.command.HasSubscribe;
import com.jd.journalq.nsr.network.command.HasSubscribeAck;
import com.jd.journalq.nsr.network.command.LeaderReport;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import com.jd.journalq.nsr.network.command.NsrConnection;
import com.jd.journalq.nsr.network.command.Register;
import com.jd.journalq.nsr.network.command.RegisterAck;
import com.jd.journalq.toolkit.concurrent.EventBus;
import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.toolkit.config.PropertySupplier;
import com.jd.journalq.toolkit.config.PropertySupplierAware;
import com.jd.journalq.toolkit.lang.Close;
import com.jd.journalq.toolkit.lang.LifeCycle;
import com.jd.journalq.toolkit.service.Service;
import com.jd.journalq.toolkit.time.SystemClock;
import com.jd.laf.extension.Type;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.SUBSCRIBE),
                new com.jd.journalq.network.command.Subscribe().subscriptions(subscriptions).clientType(clientType));
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
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.UN_SUBSCRIBE), new UnSubscribe().subscriptions(subscriptions));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("unSubscribe error request {},response {}", request, response);
            throw new RuntimeException(String.format("unSubscribe error request {},response {}", request, response));
        }
    }

    @Override
    public boolean hasSubscribe(String app, Subscription.Type subscribe) {
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.HAS_SUBSCRIBE), new HasSubscribe().app(app).subscribe(subscribe));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("hasSubscribe error request {},response {}", request, response);
            throw new RuntimeException(String.format("hasSubscribe error request {},response {}", request, response));
        }
        return ((HasSubscribeAck) response.getPayload()).isHave();
    }

    @Override
    public void leaderReport(TopicName topic, int partitionGroup, int leaderBrokerId, Set<Integer> isrId, int termId) {
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.LEADER_REPORT),
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
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_BROKER), new GetBroker().brokerId(brokerId));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getBroker error request {},response {}", request, response);
            throw new RuntimeException(String.format("getBroker error request {},response {}", request, response));
        }
        return ((GetBrokerAck) response.getPayload()).getBroker();
    }

    @Override
    public List<Broker> getAllBrokers() {
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_ALL_BROKERS), new GetAllBrokers());
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getAllBrokers error request {},response {}", request, response);
            throw new RuntimeException(String.format("getAllBrokers error request {},response {}", request, response));
        }
        return ((GetAllBrokersAck) response.getPayload()).getBrokers();
    }

    @Override
    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.ADD_TOPIC), new AddTopic().topic(topic).partitiionGroups(partitionGroups));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("leaderReport error request {},response {}", request, response);
            throw new RuntimeException(String.format("leaderReport error request {},response {}", request, response));
        }
    }

    @Override
    public TopicConfig getTopicConfig(TopicName topic) {
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_TOPICCONFIG), new GetTopicConfig().topic(topic));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getTopicConfig error request {},response {}", request, response);
            throw new RuntimeException(String.format("getTopicConfig error request {},response {}", request, response));
        }
        TopicConfig topicConfig = ((GetTopicConfigAck) response.getPayload()).getTopicConfig();
        return topicConfig;
    }

    @Override
    public Set<String> getAllTopics() {
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_ALL_TOPICS), new GetAllTopics());
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getAllTopics error request {},response {}", request, response);
            throw new RuntimeException(String.format("getAllTopics error request {},response {}", request, response));
        }
        return ((GetAllTopicsAck) response.getPayload()).getTopicNames();
    }

    @Override
    public Set<String> getTopics(String app, Subscription.Type subscription) {
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_TOPICS), new GetTopics().app(app).subscribeType(subscription.getValue()));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getTopics error request {},response {}", request, response);
            throw new RuntimeException(String.format("getTopics error request {},response {}", request, response));
        }
        return ((GetTopicsAck) response.getPayload()).getTopics();
    }

    @Override
    public Map<TopicName, TopicConfig> getTopicConfigByBroker(Integer brokerId) {
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_TOPICCONFIGS_BY_BROKER), new GetTopicConfigByBroker().brokerId(brokerId));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getTopicConfigByBroker error request {},response {}", request, response);
            throw new RuntimeException(String.format("getTopicConfigByBroker error request {},response {}", request, response));
        }
        return ((GetTopicConfigByBrokerAck) response.getPayload()).getTopicConfigs();
    }

    @Override
    public Broker register(Integer brokerId, String brokerIp, Integer port) {
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.REGISTER), new Register().brokerId(brokerId).brokerIp(brokerIp).port(port));
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
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_PRODUCER_BY_TOPIC_AND_APP), new GetProducerByTopicAndApp().topic(topic).app(app));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getProducerByTopicAndApp error request {},response {}", request, response);
            throw new RuntimeException(String.format("getProducerByTopicAndApp error request {},response {}", request, response));
        }
        return ((GetProducerByTopicAndAppAck) response.getPayload()).getProducer();
    }

    @Override
    public Consumer getConsumerByTopicAndApp(TopicName topic, String app) {
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_CONSUMER_BY_TOPIC_AND_APP), new GetConsumerByTopicAndApp().topic(topic).app(app));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getConsumerByTopicAndApp error request {},response {}", request, response);
            throw new RuntimeException(String.format("getConsumerByTopicAndApp error request {},response {}", request, response));
        }
        return ((GetConsumerByTopicAndAppAck) response.getPayload()).getConsumer();
    }

    @Override
    public Map<TopicName, TopicConfig> getTopicConfigByApp(String app, Subscription.Type subscribe) {
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_TOPICCONFIGS_BY_APP), new GetTopicConfigByApp().subscribe(subscribe).app(app));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getTopicConfigByApp error request {},response {}", request, response);
            throw new RuntimeException(String.format("getTopicConfigByApp error request {},response {}", request, response));
        }
        return ((GetTopicConfigByAppAck) response.getPayload()).getTopicConfigs();
    }

    @Override
    public DataCenter getDataCenter(String ip) {
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_DATACENTER), new GetDataCenter().ip(ip));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getTopicConfigByApp error request {},response {}", request, response);
            throw new RuntimeException(String.format("getTopicConfigByApp error request {},response {}", request, response));
        }
        return ((GetDataCenterAck) response.getPayload()).getDataCenter();
    }

    @Override
    public String getConfig(String group, String key) {
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_CONFIG), new GetConfig().group(group).key(key));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getConfig error request {},response {}", request, response);
            throw new RuntimeException(String.format("getConfig error request {},response {}", request, response));
        }
        return ((GetConfigAck) response.getPayload()).getValue();
    }

    @Override
    public List<Config> getAllConfigs() {
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_ALL_CONFIG), new GetAllConfigs());
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getAllConfigs error request {},response {}", request, response);
            throw new RuntimeException(String.format("getAllConfigs error request {},response {}", request, response));
        }
        return ((GetAllConfigsAck) response.getPayload()).getConfigs();
    }

    @Override
    public List<Broker> getBrokerByRetryType(String retryType) {
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_BROKER_BY_RETRYTYPE), new GetBrokerByRetryType().retryType(retryType));
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
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_CONSUMER_BY_TOPIC), new GetConsumerByTopic().topic(topic));
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
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_PRODUCER_BY_TOPIC), new GetProducerByTopic().topic(topic));
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
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_REPLICA_BY_BROKER), new GetReplicaByBroker().brokerId(brokerId));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getReplicaByBroker error request {},response {}", request, response);
            throw new RuntimeException(String.format("getReplicaByBroker error request {},response {}", request, response));
        }
        return ((GetReplicaByBrokerAck) response.getPayload()).getReplicas();
    }

    @Override
    public AppToken getAppToken(String app, String token) {
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.GET_APP_TOKEN), new GetAppToken().app(app).token(token));
        Command response = send(request);
        if (!response.isSuccess()) {
            logger.error("getAppToken error request {},response {}", request, response);
            throw new RuntimeException(String.format("getAppToken error request {},response {}", request, response));
        }
        return ((GetAppTokenAck) response.getPayload()).getAppToken();
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
        // TODO 临时监控
        long startTime = SystemClock.now();
        try {
            return clientTransport.getOrCreateTransport().sync(request,10000);
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
        try {
            clientTransport.getOrCreateTransport().async(request,10000, callback);
        } catch (TransportException exception) {
            logger.error("send command to nameServer error request {}", request);
            throw exception;
        }
    }

    private Command registerToNsr() throws TransportException {
        if (null == broker) {
            return null;
        }
        Command request = new Command(new JournalqHeader(Direction.REQUEST, NsrCommandType.CONNECT), new NsrConnection().brokerId(broker.getId()));
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
            this.transportClient = new NsrTransportClientFactory(nameService).create(config.getClientConfig());
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
                        transport = transportClient.createTransport(nameServiceConfig.getNamserverAddress());
                        transports.set(transport);
                    }
                    logger.info("create transport connect to nameServer [{}]", nameServiceConfig.getNamserverAddress());
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
