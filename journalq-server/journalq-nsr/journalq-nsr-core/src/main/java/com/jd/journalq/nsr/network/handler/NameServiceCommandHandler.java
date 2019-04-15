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
package com.jd.journalq.nsr.network.handler;

import com.jd.journalq.domain.*;
import com.jd.journalq.event.NameServerEvent;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.command.*;
import com.jd.journalq.network.event.TransportEvent;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.TransportAttribute;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Direction;
import com.jd.journalq.network.transport.command.Types;
import com.jd.journalq.network.transport.support.DefaultTransportAttribute;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.nsr.message.MessageListener;
import com.jd.journalq.nsr.network.NsrCommandHandler;
import com.jd.journalq.nsr.network.command.*;
import com.jd.journalq.toolkit.concurrent.EventListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wylixiaobin
 * Date: 2019/3/14
 */
public class NameServiceCommandHandler  implements NsrCommandHandler, Types,com.jd.laf.extension.Type<String>,EventListener<TransportEvent> {
    private static final Logger logger = LoggerFactory.getLogger(NameServiceCommandHandler.class);

    private NameService nameService;
    private final Map<Integer, Transport> nsrClients = new ConcurrentHashMap<>();
    @Override
    public int[] types() {
        return new int[]{
                NsrCommandType
                        .ADD_TOPIC,
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
        switch (command.getHeader().getType()){
            case NsrCommandType
                    .ADD_TOPIC:
                AddTopic addTopic = (AddTopic) command.getPayload();
                nameService.addTopic(addTopic.getTopic(),addTopic.getPartitionGroups());
                response = BooleanAck.build();
                break;
            case NsrCommandType.GET_ALL_BROKERS:
                response = new Command(new GetAllBrokersAck().brokers(nameService.getAllBrokers()));
                break;
            case NsrCommandType.GET_ALL_CONFIG:
                response = new Command(new GetAllConfigsAck().configs(nameService.getAllConfigs()));
                break;
            case NsrCommandType.GET_ALL_TOPICS:
                response = new Command(new GetAllTopicsAck().topicNames(nameService.getAllTopics()));
                break;
            case NsrCommandType.GET_APP_TOKEN:
                GetAppToken getAppToken = (GetAppToken) command.getPayload();
                AppToken appToken = nameService.getAppToken(getAppToken.getApp(),getAppToken.getToken());
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
                String value = nameService.getConfig(getConfig.getGroup(),getConfig.getKey());
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
                Map<TopicName, TopicConfig> topicConfigs = nameService.getTopicConfigByApp(getTopicConfigByApp.getApp(),getTopicConfigByApp.getSubscribe());
                response = new Command(new GetTopicConfigByAppAck().topicConfigs(topicConfigs));
                break;
            case NsrCommandType.GET_TOPICCONFIGS_BY_BROKER:
                GetTopicConfigByBroker getTopicConfigByBroker = (GetTopicConfigByBroker) command.getPayload();
                Map<TopicName,TopicConfig> topicConfigByBroker = nameService.getTopicConfigByBroker(getTopicConfigByBroker.getBrokerId());
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
                if(StringUtils.isBlank(getTopics.getApp())){
                    topicNames = nameService.getAllTopics();
                }else {
                    topicNames = nameService.getTopics(getTopics.getApp(), Subscription.Type.valueOf((byte)getTopics.getSubscribeType()));
                }
                response = new Command(new JMQHeader(Direction.RESPONSE,command.getHeader().getType()==NsrCommandType.MQTT_GET_TOPICS?NsrCommandType.MQTT_GET_TOPICS_ACK:NsrCommandType.GET_TOPICS_ACK),new GetTopicsAck().topics(topicNames));
                break;
            case NsrCommandType.HAS_SUBSCRIBE:
                HasSubscribe hasSubscribe = (HasSubscribe) command.getPayload();
                response = new Command(new HasSubscribeAck().have(nameService.hasSubscribe(hasSubscribe.getApp(),hasSubscribe.getSubscribe())));
                break;
            case NsrCommandType.LEADER_REPORT:
                LeaderReport leaderReport = (LeaderReport) command.getPayload();
                nameService.leaderReport(leaderReport.getTopic(), leaderReport.getPartitionGroup(), leaderReport.getLeaderBrokerId(), leaderReport.getIsrId(), leaderReport.getTermId());
                response = new Command(new LeaderReportAck());
                break;
            case NsrCommandType.REGISTER:
                Register register = (Register) command.getPayload();
                Broker brokerRegister = nameService.register(register.getBrokerId(),register.getBrokerIp(),register.getPort());
                if(null!=brokerRegister){
                    fillTransportBrokerId(transport,brokerRegister.getId());
                }
                response = new Command(new RegisterAck().broker(brokerRegister));
                break;
            case NsrCommandType.SUBSCRIBE:
                Subscribe subscribe = (Subscribe) command.getPayload();
                List<TopicConfig> subscribeTopicConfigs = nameService.subscribe(subscribe.getSubscriptions(), subscribe.getClientType());
                response = new Command(new JMQHeader(Direction.RESPONSE, NsrCommandType.SUBSCRIBE_ACK), new SubscribeAck().topicConfigs(subscribeTopicConfigs));
                break;
            case NsrCommandType.UN_SUBSCRIBE:
                UnSubscribe unSubscribe = (UnSubscribe) command.getPayload();
                nameService.unSubscribe(unSubscribe.getSubscriptions());
                response = BooleanAck.build();
                break;
            case NsrCommandType.AUTHORIZATION:
                Authorization authorization = (Authorization) command.getPayload();
                Date now = Calendar.getInstance().getTime();
                AppToken appTokenforAuth = nameService.getAppToken(authorization.getApp(),authorization.getToken());
                response = null!=appTokenforAuth&&appTokenforAuth.getEffectiveTime().before(now)&&appTokenforAuth.getExpirationTime().after(now)?BooleanAck.build():BooleanAck.build(JMQCode.CN_AUTHENTICATION_ERROR);
                break;
            case NsrCommandType.CONNECT:
                Integer brokerId = ((NsrConnection)command.getPayload()).getBrokerId();
                fillTransportBrokerId(transport,brokerId);
                response = BooleanAck.build();
                break;
            default:
                response = BooleanAck.build(JMQCode.CN_UNKNOWN_ERROR,"unRecognize command ");
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
        nsrClients.put(brokerId,transport);
        logger.info("{} online",brokerId);
    }

    @Override
    public void onEvent(TransportEvent event) {
        Transport transport = event.getTransport();
        Integer brokerId = transport.attr().get("broker.id");
        if(null==brokerId||0==brokerId){
            return;
        }
        switch (event.getType()) {
            case CONNECT:
                    nsrClients.put(brokerId,transport);
                    logger.info("{} online",brokerId);
                    break;
            case EXCEPTION:
            case CLOSE:
                nsrClients.remove(Integer.valueOf(brokerId));
                logger.info("{} offline",brokerId);
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
            logger.info("event[{}]");
            Transport transport = nsrClients.get(event.getBrokerId());
                if(null!=transport) {
                    transport.sync(new Command(new JMQHeader(Direction.REQUEST, NsrCommandType.PUSH_NAMESERVER_EVENT), new PushNameServerEvent().event(event)));
                    logger.info("event[{}] send to [{}] success", event,event.getBrokerId());
                }
            }catch (Exception e){
                logger.error("push event to [{}] error",event.getBrokerId(),e);
            }
        }
    }
}
