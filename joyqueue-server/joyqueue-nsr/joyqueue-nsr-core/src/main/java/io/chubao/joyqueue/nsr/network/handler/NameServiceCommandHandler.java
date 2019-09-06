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
package io.chubao.joyqueue.nsr.network.handler;

import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.Subscription;
import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.NameServerEvent;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.Authorization;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.command.GetTopics;
import io.chubao.joyqueue.network.command.GetTopicsAck;
import io.chubao.joyqueue.network.command.Subscribe;
import io.chubao.joyqueue.network.command.SubscribeAck;
import io.chubao.joyqueue.network.command.UnSubscribe;
import io.chubao.joyqueue.network.event.TransportEvent;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.TransportAttribute;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.CommandCallback;
import io.chubao.joyqueue.network.transport.command.Direction;
import io.chubao.joyqueue.network.transport.command.Types;
import io.chubao.joyqueue.network.transport.support.DefaultTransportAttribute;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.nsr.config.NameServiceConfig;
import io.chubao.joyqueue.nsr.message.MessageListener;
import io.chubao.joyqueue.nsr.network.NsrCommandHandler;
import io.chubao.joyqueue.nsr.network.command.AddTopic;
import io.chubao.joyqueue.nsr.network.command.GetAllBrokersAck;
import io.chubao.joyqueue.nsr.network.command.GetAllConfigsAck;
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
import io.chubao.joyqueue.nsr.network.command.LeaderReportAck;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.chubao.joyqueue.nsr.network.command.NsrConnection;
import io.chubao.joyqueue.nsr.network.command.PushNameServerEvent;
import io.chubao.joyqueue.nsr.network.command.Register;
import io.chubao.joyqueue.nsr.network.command.RegisterAck;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wylixiaobin
 * Date: 2019/3/14
 */
public class NameServiceCommandHandler implements NsrCommandHandler, Types, com.jd.laf.extension.Type<String>, EventListener<TransportEvent>, PropertySupplierAware {
    private static final Logger logger = LoggerFactory.getLogger(NameServiceCommandHandler.class);

    private NameService nameService;
    private NameServiceConfig config;
    private final Map<Integer, Transport> nsrClients = new ConcurrentHashMap<>();

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.config = new NameServiceConfig(supplier);
    }

    @Override
    public int[] types() {
        return new int[]{
                NsrCommandType.ADD_TOPIC,
                NsrCommandType.GET_ALL_BROKERS,
                NsrCommandType.GET_ALL_CONFIG,
                NsrCommandType.GET_ALL_TOPICS,
                NsrCommandType.GET_APP_TOKEN,
                NsrCommandType.GET_BROKER_BY_RETRYTYPE,
                NsrCommandType.GET_BROKER,
                NsrCommandType.GET_CONFIG,
                NsrCommandType.GET_CONSUMER_BY_TOPIC_AND_APP,
                NsrCommandType.GET_CONSUMER_BY_TOPIC,
                NsrCommandType.GET_DATACENTER,
                NsrCommandType.GET_PRODUCER_BY_TOPIC_AND_APP,
                NsrCommandType.GET_PRODUCER_BY_TOPIC,
                NsrCommandType.GET_REPLICA_BY_BROKER,
                NsrCommandType.GET_TOPICCONFIGS_BY_APP,
                NsrCommandType.GET_TOPICCONFIGS_BY_BROKER,
                NsrCommandType.GET_TOPICCONFIG,
                NsrCommandType.GET_TOPICS,
                NsrCommandType.HAS_SUBSCRIBE,
                NsrCommandType.LEADER_REPORT,
                NsrCommandType.MQTT_GET_TOPICS,
                NsrCommandType.REGISTER,
                NsrCommandType.SUBSCRIBE,
                NsrCommandType.UN_SUBSCRIBE,
                NsrCommandType.AUTHORIZATION,
                NsrCommandType.CONNECT
        };
    }

    @Override
    public Command handle(Transport transport, Command command) {
        Command response = null;
        switch (command.getHeader().getType()) {
            case NsrCommandType.ADD_TOPIC:
                AddTopic addTopic = (AddTopic) command.getPayload();
                nameService.addTopic(addTopic.getTopic(), addTopic.getPartitionGroups());
                response = BooleanAck.build();
                break;
            case NsrCommandType.GET_ALL_BROKERS:
                response = new Command(new GetAllBrokersAck().brokers(nameService.getAllBrokers()));
                break;
            case NsrCommandType.GET_ALL_CONFIG:
                response = new Command(new GetAllConfigsAck().configs(nameService.getAllConfigs()));
                break;
            case NsrCommandType.GET_ALL_TOPICS:
                response = new Command(new GetAllTopicsAck().topicNames(nameService.getAllTopicCodes()));
                break;
            case NsrCommandType.GET_APP_TOKEN:
                GetAppToken getAppToken = (GetAppToken) command.getPayload();
                AppToken appToken = nameService.getAppToken(getAppToken.getApp(), getAppToken.getToken());
                response = new Command(new GetAppTokenAck().appToken(appToken));
                break;
            case NsrCommandType.GET_BROKER_BY_RETRYTYPE:
                GetBrokerByRetryType getBrokerByRetryType = (GetBrokerByRetryType) command.getPayload();
                List<Broker> brokers = nameService.getBrokerByRetryType(getBrokerByRetryType.getRetryType());
                response = new Command(new GetBrokerByRetryTypeAck().brokers(brokers));
                break;
            case NsrCommandType.GET_BROKER:
                GetBroker getBroker = (GetBroker) command.getPayload();
                Broker broker = nameService.getBroker(getBroker.getBrokerId());
                response = new Command(new GetBrokerAck().broker(broker));
                break;
            case NsrCommandType.GET_CONFIG:
                GetConfig getConfig = (GetConfig) command.getPayload();
                String value = nameService.getConfig(getConfig.getGroup(), getConfig.getKey());
                response = new Command(new GetConfigAck().value(value));
                break;
            case NsrCommandType.GET_CONSUMER_BY_TOPIC_AND_APP:
                GetConsumerByTopicAndApp getConsumerByTopicAndApp = (GetConsumerByTopicAndApp) command.getPayload();
                Consumer consumer = nameService.getConsumerByTopicAndApp(getConsumerByTopicAndApp.getTopic(), getConsumerByTopicAndApp.getApp());
                response = new Command(new GetConsumerByTopicAndAppAck().consumer(consumer));
                break;
            case NsrCommandType.GET_CONSUMER_BY_TOPIC:
                GetConsumerByTopic getConsumerByTopic = (GetConsumerByTopic) command.getPayload();
                List<Consumer> consumers = nameService.getConsumerByTopic(getConsumerByTopic.getTopic());
                response = new Command(new GetConsumerByTopicAck().consumers(consumers));
                break;
            case NsrCommandType.GET_DATACENTER:
                GetDataCenter getDataCenter = (GetDataCenter) command.getPayload();
                DataCenter dataCenter = nameService.getDataCenter(getDataCenter.getIp());
                response = new Command(new GetDataCenterAck().dataCenter(dataCenter));
                break;
            case NsrCommandType.GET_PRODUCER_BY_TOPIC_AND_APP:
                GetProducerByTopicAndApp getProducerByTopicAndApp = (GetProducerByTopicAndApp) command.getPayload();
                Producer producer = nameService.getProducerByTopicAndApp(getProducerByTopicAndApp.getTopic(), getProducerByTopicAndApp.getApp());
                response = new Command(new GetProducerByTopicAndAppAck().producer(producer));
                break;
            case NsrCommandType.GET_PRODUCER_BY_TOPIC:
                GetProducerByTopic getProducerByTopic = (GetProducerByTopic) command.getPayload();
                List<Producer> producers = nameService.getProducerByTopic(getProducerByTopic.getTopic());
                response = new Command(new GetProducerByTopicAck().producers(producers));
                break;
            case NsrCommandType.GET_REPLICA_BY_BROKER:
                GetReplicaByBroker getReplicaByBroker = (GetReplicaByBroker) command.getPayload();
                List<Replica> replicas = nameService.getReplicaByBroker(getReplicaByBroker.getBrokerId());
                response = new Command(new GetReplicaByBrokerAck().replicas(replicas));
                break;
            case NsrCommandType.GET_TOPICCONFIGS_BY_APP:
                GetTopicConfigByApp getTopicConfigByApp = (GetTopicConfigByApp) command.getPayload();
                Map<TopicName, TopicConfig> topicConfigs = nameService.getTopicConfigByApp(getTopicConfigByApp.getApp(), getTopicConfigByApp.getSubscribe());
                response = new Command(new GetTopicConfigByAppAck().topicConfigs(topicConfigs));
                break;
            case NsrCommandType.GET_TOPICCONFIGS_BY_BROKER:
                GetTopicConfigByBroker getTopicConfigByBroker = (GetTopicConfigByBroker) command.getPayload();
                Map<TopicName, TopicConfig> topicConfigByBroker = nameService.getTopicConfigByBroker(getTopicConfigByBroker.getBrokerId());
                response = new Command(new GetTopicConfigByBrokerAck().topicConfigs(topicConfigByBroker));
                break;
            case NsrCommandType.GET_TOPICCONFIG:
                GetTopicConfig getTopicConfig = (GetTopicConfig) command.getPayload();
                TopicConfig topicConfig = nameService.getTopicConfig(getTopicConfig.getTopic());
                response = new Command(new GetTopicConfigAck().topicConfig(topicConfig));
                break;
            case NsrCommandType.GET_TOPICS:
            case NsrCommandType.MQTT_GET_TOPICS:
                GetTopics getTopics = (GetTopics) command.getPayload();
                Set<String> topicNames = null;
                if (StringUtils.isBlank(getTopics.getApp())) {
                    topicNames = nameService.getAllTopicCodes();
                } else {
                    topicNames = nameService.getTopics(getTopics.getApp(), Subscription.Type.valueOf((byte) getTopics.getSubscribeType()));
                }
                response = new Command(new JoyQueueHeader(Direction.RESPONSE,
                        command.getHeader().getType() == NsrCommandType.MQTT_GET_TOPICS ? NsrCommandType.MQTT_GET_TOPICS_ACK : NsrCommandType.GET_TOPICS_ACK),
                        new GetTopicsAck().topics(topicNames));
                break;
            case NsrCommandType.HAS_SUBSCRIBE:
                HasSubscribe hasSubscribe = (HasSubscribe) command.getPayload();
                response = new Command(new HasSubscribeAck().have(nameService.hasSubscribe(hasSubscribe.getApp(), hasSubscribe.getSubscribe())));
                break;
            case NsrCommandType.LEADER_REPORT:
                LeaderReport leaderReport = (LeaderReport) command.getPayload();
                logger.info("Name service receive leader report command {} from {}", leaderReport, transport.remoteAddress());
                nameService.leaderReport(leaderReport.getTopic(), leaderReport.getPartitionGroup(), leaderReport.getLeaderBrokerId(), leaderReport.getIsrId(), leaderReport.getTermId());
                response = new Command(new LeaderReportAck());
                break;
            case NsrCommandType.REGISTER:
                Register register = (Register) command.getPayload();
                Broker brokerRegister = nameService.register(register.getBrokerId(), register.getBrokerIp(), register.getPort());
                if (null != brokerRegister) {
                    fillTransportBrokerId(transport, brokerRegister.getId());
                    response = new Command(new RegisterAck().broker(brokerRegister));
                } else {
                    response = BooleanAck.build(JoyQueueCode.NSR_REGISTER_ERR_BROKER_NOT_EXIST);
                }
                break;
            case NsrCommandType.SUBSCRIBE:
                Subscribe subscribe = (Subscribe) command.getPayload();
                List<TopicConfig> subscribeTopicConfigs = nameService.subscribe(subscribe.getSubscriptions(), subscribe.getClientType());
                response = new Command(new JoyQueueHeader(Direction.RESPONSE, NsrCommandType.SUBSCRIBE_ACK), new SubscribeAck().topicConfigs(subscribeTopicConfigs));
                break;
            case NsrCommandType.UN_SUBSCRIBE:
                UnSubscribe unSubscribe = (UnSubscribe) command.getPayload();
                nameService.unSubscribe(unSubscribe.getSubscriptions());
                response = BooleanAck.build();
                break;
            case NsrCommandType.AUTHORIZATION:
                Authorization authorization = (Authorization) command.getPayload();
                Date now = Calendar.getInstance().getTime();
                AppToken appTokenforAuth = nameService.getAppToken(authorization.getApp(), authorization.getToken());
                response = ((null != appTokenforAuth) && appTokenforAuth.getEffectiveTime().before(now) && appTokenforAuth.getExpirationTime().after(now)) ?
                        BooleanAck.build() :
                        BooleanAck.build(JoyQueueCode.CN_AUTHENTICATION_ERROR);
                break;
            case NsrCommandType.CONNECT:
                Integer brokerId = ((NsrConnection) command.getPayload()).getBrokerId();
                fillTransportBrokerId(transport, brokerId);
                response = BooleanAck.build();
                break;
            default:
                response = BooleanAck.build(JoyQueueCode.CN_UNKNOWN_ERROR, "unRecognize command ");
                break;
        }
        return response;
    }

    @Override
    public void setNameService(NameService nameService) {
        this.nameService = nameService;
        this.nameService.addListener(new PushMetaEventListener());
    }

    @Override
    public String type() {
        return SERVER_TYPE;
    }

    private void fillTransportBrokerId(Transport transport, int brokerId) {
        TransportAttribute attribute = transport.attr();
        if (attribute == null) {
            attribute = new DefaultTransportAttribute();
            transport.attr(attribute);
        }
        attribute.set("broker.id", brokerId);
        nsrClients.put(brokerId, transport);
        logger.info("{} online", brokerId);
    }

    @Override
    public void onEvent(TransportEvent event) {
        Transport transport = event.getTransport();
        Integer brokerId = transport.attr().get("broker.id");
        if (null == brokerId || 0 == brokerId) {
            return;
        }
        switch (event.getType()) {
            case CONNECT:
                nsrClients.put(brokerId, transport);
                logger.info("{} online", brokerId);
                break;
            case EXCEPTION:
            case CLOSE:
                nsrClients.remove(Integer.valueOf(brokerId));
                logger.info("{} offline", brokerId);
                break;
            default:
                break;
        }
    }

    /**
     * MetaDataListener
     */
    protected class PushMetaEventListener implements MessageListener<NameServerEvent> {
        private final Logger logger = LoggerFactory.getLogger(PushMetaEventListener.class);

        @Override
        public void onEvent(NameServerEvent event) {
            try {
                logger.info("will publish event [{}]", event);
                if (event == null || event.getBrokerId() == null) {
                    logger.warn("broker is null.");
                } else if (nsrClients == null || nsrClients.isEmpty()) {
                    logger.warn("nsr client is null.");
                } else {
                    Integer brokerId = event.getBrokerId();
                    if (event.getBrokerId() == NameServerEvent.BROKER_ID_ALL_BROKER) {
                        sendEvent(event, nsrClients.values().toArray(new Transport[nsrClients.size()]));
                    } else {
                        Transport transport = nsrClients.get(brokerId);
                        if (transport != null) {
                            sendEvent(event, transport);
                        } else {
                            logger.warn("transport is null.brokerId[{}]", brokerId);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("push event to [{}] error", event.getBrokerId(), e);
            }
        }


        private void sendEvent(NameServerEvent event, Transport... transports) {
            if (transports == null) {
                return;
            }

            for (Transport transport : transports) {
                transport.async(new Command(new JoyQueueHeader(Direction.REQUEST, NsrCommandType.PUSH_NAMESERVER_EVENT), new PushNameServerEvent().event(event)), new CommandCallback() {
                    @Override
                    public void onSuccess(Command request, Command response) {
                        logger.info("event[{}] send to [{}] success", event, event.getBrokerId());
                    }

                    @Override
                    public void onException(Command request, Throwable cause) {
                        logger.info("event[{}] send to [{}] failure.", event, event.getBrokerId());
                    }
                });
            }
        }
    }
}
