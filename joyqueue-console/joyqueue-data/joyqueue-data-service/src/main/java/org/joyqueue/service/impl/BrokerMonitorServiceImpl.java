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
package org.joyqueue.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.async.BrokerClusterQuery;
import org.joyqueue.async.BrokerMonitorClusterQuery;
import org.joyqueue.async.RetrieveProvider;
import org.joyqueue.convert.CodeConverter;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.exception.ServiceException;
import org.joyqueue.manage.PartitionGroupMetric;
import org.joyqueue.manage.PartitionGroupPosition;
import org.joyqueue.manage.PartitionMetric;
import org.joyqueue.manage.PartitionPosition;
import org.joyqueue.model.BrokerMetadata;
import org.joyqueue.model.domain.Broker;
import org.joyqueue.model.domain.BrokerClient;
import org.joyqueue.model.domain.BrokerMonitorRecord;
import org.joyqueue.model.domain.ConnectionMonitorInfoWithIp;
import org.joyqueue.model.domain.Subscribe;
import org.joyqueue.model.domain.SubscribeType;
import org.joyqueue.model.domain.TopicPartitionGroup;
import org.joyqueue.monitor.ArchiveMonitorInfo;
import org.joyqueue.monitor.Client;
import org.joyqueue.monitor.ConnectionMonitorDetailInfo;
import org.joyqueue.monitor.ConnectionMonitorInfo;
import org.joyqueue.monitor.ConsumerMonitorInfo;
import org.joyqueue.monitor.ConsumerPartitionGroupMonitorInfo;
import org.joyqueue.monitor.ConsumerPartitionMonitorInfo;
import org.joyqueue.monitor.DeQueueMonitorInfo;
import org.joyqueue.monitor.EnQueueMonitorInfo;
import org.joyqueue.monitor.PartitionGroupMonitorInfo;
import org.joyqueue.monitor.PendingMonitorInfo;
import org.joyqueue.monitor.ProducerMonitorInfo;
import org.joyqueue.monitor.ProducerPartitionGroupMonitorInfo;
import org.joyqueue.monitor.ProducerPartitionMonitorInfo;
import org.joyqueue.monitor.RestResponse;
import org.joyqueue.monitor.RetryMonitorInfo;
import org.joyqueue.other.HttpRestService;
import org.joyqueue.service.BrokerMonitorService;
import org.joyqueue.service.BrokerRestUrlMappingService;
import org.joyqueue.service.LeaderService;
import org.joyqueue.service.TopicPartitionGroupService;
import org.joyqueue.util.HttpUtil;
import org.joyqueue.util.NullUtil;
import org.joyqueue.util.UrlEncoderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.joyqueue.exception.ServiceException.INTERNAL_SERVER_ERROR;
import static org.joyqueue.exception.ServiceException.NOT_FOUND;
import static org.joyqueue.model.domain.Consumer.CONSUMER_TYPE;
import static org.joyqueue.model.domain.Producer.PRODUCER_TYPE;
import static org.joyqueue.util.JSONParser.parse;

@Service("brokerMonitorService")
public class BrokerMonitorServiceImpl implements BrokerMonitorService {
    private Logger logger= LoggerFactory.getLogger(BrokerMonitorServiceImpl.class);
    private static final long TIMEOUT=60000;

    @Resource(type = BrokerMonitorClusterQuery.class)
    private BrokerClusterQuery<Subscribe> brokerCluster;
    @Autowired
    private TopicPartitionGroupService partitionGroupService;

    @Autowired
    private LeaderService leaderService;

    @Autowired(required = false)
    private HttpRestService httpRestService;

    @Autowired
    private BrokerRestUrlMappingService urlMappingService;

    @Override
    public boolean removeBrokerMonitorConnections(Subscribe subscribe,Integer brokerId) {
        this.checkArgument(subscribe);
        List<Broker> brokers=new ArrayList<>();
        if(subscribe.getType().value()==PRODUCER_TYPE){
            Future<Map<String,String >> resultFuture= brokerCluster.asyncDeleteOnBroker(brokerId,subscribe, new RetrieveProvider<Subscribe>() {
                @Override
                public String getKey(Broker broker, PartitionGroup partitionGroup,short partition , Subscribe condition) {
                    brokers.add(broker);
                    return broker.getIp()+":"+broker.getPort();
                }
                 @Override
                 public String getPath(String pathTemplate, PartitionGroup partitionGroup,short partition ,Subscribe condition) {
                     return String.format(pathTemplate, UrlEncoderUtil.encodeParam(CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName(),
                             CodeConverter.convertApp(subscribe.getApp(),subscribe.getSubscribeGroup())));
                 }
            },"removeProducersConnections","monitor on broker");
        }
        else if(subscribe.getType().value()==CONSUMER_TYPE){
            Future<Map<String,String >> resultFuture= brokerCluster.asyncDeleteOnBroker(brokerId,subscribe, new RetrieveProvider<Subscribe>() {
                @Override
                public String getKey(Broker broker, PartitionGroup partitionGroup,short partition , Subscribe condition) {
                    brokers.add(broker);
                    return broker.getIp()+":"+broker.getPort();
                }
                @Override
                public String getPath(String pathTemplate, PartitionGroup partitionGroup,short partition ,Subscribe condition) {
                    return String.format(pathTemplate, UrlEncoderUtil.encodeParam(CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName(),
                            CodeConverter.convertApp(subscribe.getApp(),subscribe.getSubscribeGroup())));
                }
            },"removeConsumersConnections","monitor on broker");
            Map<String/*request key*/, String/*response*/> resultMap= brokerCluster.get(resultFuture,TIMEOUT,TimeUnit.MILLISECONDS);
        }
        return brokers.size()>0;
    }

    @Override
    public List<BrokerMonitorRecord> findMonitorOnBroker(Subscribe subscribe) {
        this.checkArgument(subscribe);
        List<BrokerMonitorRecord> monitorRecords=new ArrayList<>();
        List<Broker> brokers=new ArrayList<>();
        Future<Map<String,String >> resultFuture= brokerCluster.asyncQueryOnBroker(subscribe, new RetrieveProvider<Subscribe>() {
            @Override
            public String getKey(Broker broker, PartitionGroup partitionGroup,short partition , Subscribe condition) {
                brokers.add(broker);
                return broker.getIp()+":"+broker.getPort();
            }
            @Override
            public String getPath(String pathTemplate,PartitionGroup partitionGroup,short partition ,Subscribe condition) {
                return  String.format(pathTemplate, UrlEncoderUtil.encodeParam(CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName(),
                        subscribe.getType()==SubscribeType.PRODUCER?subscribe.getApp().getCode():CodeConverter.convertApp(subscribe.getApp(),
                                subscribe.getSubscribeGroup()), subscribe.getType().name().toLowerCase()));
            }
        },"appMonitor" ,"monitor on broker");
        Map<String/*request key*/, String/*response*/> resultMap= brokerCluster.get(resultFuture,TIMEOUT,TimeUnit.MILLISECONDS);
        if(resultMap.size()!=brokers.size()) {
            logger.info("missing some of monitor on broker ,ignore!");
            return monitorRecords;
        }
        BrokerMonitorRecord record;
        ProducerMonitorInfo producerMonitorInfo;
        ConsumerMonitorInfo consumerMonitorInfo;
        RestResponse<ConsumerMonitorInfo> restConsumeMonitor;
        RestResponse<ProducerMonitorInfo> restProducerMonitor;
        try {
            String r;
            String hostWithPort;
            for(Broker b:brokers){
                hostWithPort=b.getIp()+":"+b.getPort();
                r= resultMap.get(hostWithPort);
                if(NullUtil.isEmpty(r)){
                    logger.info(String.format("ignore %s monitor on broker %s ",JSON.toJSON(subscribe),b.getIp()));
                    continue;
                }
                record= new BrokerMonitorRecord();
                record.setIp(hostWithPort);
                switch (subscribe.getType().value()) {
                    case CONSUMER_TYPE:
                        restConsumeMonitor=parse(r,RestResponse.class, ConsumerMonitorInfo.class,false);
                        consumerMonitorInfo = restConsumeMonitor.getData();
                        record.setConnections(consumerMonitorInfo.getConnections());
                        record.setRetry(consumerMonitorInfo.getRetry());
                        record.setDeQuence(consumerMonitorInfo.getDeQueue());
                        record.setPending( consumerMonitorInfo.getPending());
                        break;
                    case PRODUCER_TYPE:
                        restProducerMonitor=parse(r,RestResponse.class, ProducerMonitorInfo.class,false);
                        producerMonitorInfo = restProducerMonitor.getData();
                        record.setConnections(producerMonitorInfo.getConnections());
                        record.setEnQuence(producerMonitorInfo.getEnQueue());
                        break;
                }
                monitorRecords.add(record);
            }
        }catch (Exception e){
            logger.info("broker asyncQueryOnBroker occurs parse exception.", e);
            throw new ServiceException(INTERNAL_SERVER_ERROR,e.getMessage(), e);
        }
        return monitorRecords;
    }

    @Override
    public BrokerMonitorRecord find(Subscribe subscribe, boolean active) {
        if(active){
            List<BrokerMonitorRecord>  brokerMonitorRecords= findMonitorOnPartitionGroupsForTopicApp(subscribe); //
            BrokerMonitorRecord brokerMonitorRecord=merge(subscribe,brokerMonitorRecords);  // not contain connection and retry info
            if(!NullUtil.isEmpty(brokerMonitorRecord)) {
                BrokerMonitorRecord retryAndConnectionInfo = ((BrokerMonitorService)AopContext.currentProxy()).find(subscribe);// optimize
                if(!NullUtil.isEmpty(retryAndConnectionInfo)) {
                    brokerMonitorRecord.setRetry(retryAndConnectionInfo.getRetry());                         // upset
                    brokerMonitorRecord.setConnections(retryAndConnectionInfo.getConnections());
                }
            }
            return brokerMonitorRecord;
        }else{
            return find(subscribe);
        }
    }


    /**
     *
     * @return  producer or consumer
     *
     */
    @Override
    public BrokerMonitorRecord find(Subscribe subscribe){
        this.checkArgument(subscribe);
        List<BrokerMonitorRecord> monitorRecords = findMonitorOnBroker(subscribe);
        return  merge(subscribe,monitorRecords);
    }


    /**
     * merge retry ,enqueue,dequeue, pending,connection monitor info
     * @return merged BrokerMonitorRecord or null if no monitor data
     **/
    public BrokerMonitorRecord merge(Subscribe subscribe,List<BrokerMonitorRecord> monitorRecords){
        if(NullUtil.isEmpty(monitorRecords)) return null;
        BrokerMonitorRecord record=monitorRecords.get(0);
        if(monitorRecords.size()>1){
            for(int i=1;i<monitorRecords.size();i++){
                switch (subscribe.getType().value()) {
                    case PRODUCER_TYPE:
                        record.setRetry(add(record.getRetry(), monitorRecords.get(i).getRetry()));
                        record.setEnQuence(add(record.getEnQuence(), monitorRecords.get(i).getEnQuence()));
                        break;
                    case CONSUMER_TYPE:
                        record.setDeQuence(add(record.getDeQuence(), monitorRecords.get(i).getDeQuence()));
                        break;
                }
                record.setConnections(record.getConnections() + monitorRecords.get(i).getConnections());
                record.setPending(add(record.getPending(),monitorRecords.get(i).getPending()));
            }
        }
        return  record;
    }




    @Override
    public List<BrokerClient> findClients(Subscribe subscribe) {
        List<BrokerClient> clients=new ArrayList<>();
        // 异步查询Broker信息
        List<Broker> brokers=new ArrayList<>();
        Future<Map<String,String >> resultFuture= brokerCluster.asyncQueryOnBroker(subscribe, new RetrieveProvider<Subscribe>() {
            @Override
            public String getKey(Broker broker,PartitionGroup partitionGroup,short partition ,Subscribe condition) {
                brokers.add(broker);
                return broker.getIp()+":"+broker.getPort();
            }
            @Override
            public String getPath(String pathTemplate,PartitionGroup partitionGroup,short partition ,Subscribe condition) {
                return  String.format(pathTemplate, UrlEncoderUtil.encodeParam(CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName(),
                        subscribe.getType()==SubscribeType.PRODUCER?subscribe.getApp().getCode():CodeConverter.convertApp(subscribe.getApp(),
                                subscribe.getSubscribeGroup()), subscribe.getType().name().toLowerCase()));
            }
        },"appClientMonitor" ,"clients on broker");
        //查找Master Brokers
        Map<String/*request key*/, String/*response*/> resultMap= brokerCluster.get(resultFuture,TIMEOUT,TimeUnit.MILLISECONDS);
        /** 任意请求出错,记录错误日志*/
        if(resultMap.size()!=brokers.size()) {
            logger.info("missing some of clients on broker ,ignore!");
        }
        RestResponse<ConnectionMonitorDetailInfo> restConnectionMonitorDetail;
        try {
            for (Map.Entry<String,String> connectionsMonitor : resultMap.entrySet()) {
                restConnectionMonitorDetail =parse(connectionsMonitor.getValue(),RestResponse.class ,ConnectionMonitorDetailInfo.class,false);
                clients.addAll(convert(connectionsMonitor.getKey(),restConnectionMonitorDetail.getData().getClients()));
            }
        }catch (Exception e){
            logger.info(" parse connection info exception.", e);
            throw new ServiceException(INTERNAL_SERVER_ERROR,e.getMessage(), e);
        }
        //mock large size result
//        if(clients.size()>0){
//           BrokerClient c= clients.get(0);
//           for(int i=0;i<1000;i++){
//               clients.add(c);
//           }
//        }
        return clients;
    }


    /**
     * client to brokerClient convert
     **/
    public List<BrokerClient>  convert(String brokerIp,List<Client> clients){
        List<BrokerClient> brokerClients=new ArrayList<>();
        BrokerClient brokerClient;
        for(Client c:clients){
            brokerClient=new BrokerClient();
            brokerClient.setIp(brokerIp);
            brokerClient.setClient(c);
            brokerClients.add(brokerClient);
        }
        return brokerClients;
    }
    @Override
    public List<BrokerMonitorRecord> findMonitorOnPartition(Subscribe subscribe) {
        checkArgument(subscribe);
        List<BrokerMonitorRecord> monitorRecords=null;
        List<Broker> brokers=new ArrayList<>();
        Future<Map<String,String >> resultFuture= brokerCluster.asyncQueryOnBroker(subscribe, new RetrieveProvider<Subscribe>() {
            @Override
            public String getKey(Broker broker,PartitionGroup partitionGroup,short partition ,Subscribe condition) {
                brokers.add(broker);
                return broker.getIp()+":"+broker.getPort();
            }
            @Override
            public String getPath(String pathTemplate,PartitionGroup partitionGroup,short partition ,Subscribe condition) {
                return  String.format(pathTemplate, UrlEncoderUtil.encodeParam(CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName(),
                        subscribe.getType()==SubscribeType.PRODUCER?subscribe.getApp().getCode():CodeConverter.convertApp(subscribe.getApp(),
                                subscribe.getSubscribeGroup()), subscribe.getType().name().toLowerCase()));
            }
        },"appPartitionMonitor" ,"partitions on broker");
        Map<String/*request key*/, String/*response*/> resultMap= brokerCluster.get(resultFuture,TIMEOUT,TimeUnit.MILLISECONDS);
        /*任一请求失败*/
        if(resultMap.size()!=brokers.size()) {
            logger.info("ignore some broker partitions");
        }

        RestResponse<List<ConsumerPartitionMonitorInfo>> restConsumePartitionMonitor;
        RestResponse<List<ProducerPartitionMonitorInfo>> restProducePartitionMonitor;
        try {
            String r;
            String hostWithPort;
            for(Broker b:brokers){
                hostWithPort=b.getIp()+":"+b.getPort();
                r= resultMap.get(hostWithPort);
                if(NullUtil.isEmpty(r)){
                    logger.info(String.format("ignore %s partitions on broker %s ",JSON.toJSON(subscribe),b.getIp()));
                    continue;
                }
                switch (subscribe.getType().value()) {
                    case CONSUMER_TYPE:
                        restConsumePartitionMonitor=parse(r,RestResponse.class,ConsumerPartitionMonitorInfo.class,true);
                        List<ConsumerPartitionMonitorInfo> consumerPartitionMonitors= restConsumePartitionMonitor.getData();
                        monitorRecords=transferConsumerPartition(consumerPartitionMonitors,hostWithPort);
                        break;
                    case PRODUCER_TYPE:
                        restProducePartitionMonitor=parse(r,RestResponse.class,ProducerPartitionMonitorInfo.class,true);
                        List<ProducerPartitionMonitorInfo> producerPartitionMonitors= restProducePartitionMonitor.getData();
                        monitorRecords=transferProducerPartition(producerPartitionMonitors,hostWithPort);
                        break;
                }
            }
        }catch (Exception e){
            logger.info("broker asyncQueryOnBroker occurs parse exception.", e);
            throw new ServiceException(INTERNAL_SERVER_ERROR,e.getMessage(), e);
        }
        if(!NullUtil.isEmpty(monitorRecords)){
            monitorRecords.sort(Comparator.comparing(e->e.getPartition()));
        }
        return monitorRecords;
    }


    @Override
    public List<BrokerMonitorRecord> findMonitorOnPartition(Subscribe subscribe, int partitionGroup) {
        List<BrokerMonitorRecord> monitorRecords=null;
        Map.Entry<PartitionGroup,Broker> partitionGroupBrokerEntry=leaderService.findPartitionGroupLeaderBrokerDetail(subscribe.getNamespace().getCode(),subscribe.getTopic().getCode(),partitionGroup);
        if(!NullUtil.isEmpty(partitionGroupBrokerEntry)){
            RestResponse<List<ConsumerPartitionMonitorInfo>> restConsumePartitionMonitor;
            RestResponse<List<ProducerPartitionMonitorInfo>> restProducePartitionMonitor;
            Broker b=partitionGroupBrokerEntry.getValue();
            String pathKey="appPartitionMonitor";

            String[] encodedArgs=UrlEncoderUtil.encodeParam(CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName(),
                    subscribe.getType()==SubscribeType.PRODUCER?subscribe.getApp().getCode():CodeConverter.convertApp(subscribe.getApp(),
                            subscribe.getSubscribeGroup()),subscribe.getType().name().toLowerCase());
            String[] args=new String[encodedArgs.length+2];
            args[0]=b.getIp();
            args[1]=String.valueOf(b.getMonitorPort());
            for(int i=2;i<args.length;i++){   // fill args
                args[i]=encodedArgs[i-2];
            }
            String hostWithPort=b.getIp()+":"+b.getPort();
            switch (subscribe.getType().value()){
                case PRODUCER_TYPE:
                    restProducePartitionMonitor=httpRestService.get(pathKey,ProducerPartitionMonitorInfo.class,true,args);
                    List<ProducerPartitionMonitorInfo> producerPartitionMonitorInfos=restProducePartitionMonitor.getData();
                    monitorRecords=transferProducerPartition(producerPartitionMonitorInfos,hostWithPort);
                    break;
                case CONSUMER_TYPE:
                    restConsumePartitionMonitor=  httpRestService.get(pathKey,ConsumerPartitionMonitorInfo.class,true,args);
                    List<ConsumerPartitionMonitorInfo> consumerPartitionMonitors=restConsumePartitionMonitor.getData();
                    monitorRecords=transferConsumerPartition(consumerPartitionMonitors,hostWithPort);
                    break;
            }
        }

        return monitorRecords==null?new ArrayList<>():monitorRecords;
    }




    /**
     *
     *  to do optimize: 查询指定partition group 所在broker 的partition 用于过滤
     *
     **/
    @Override
    public List<BrokerMonitorRecord> findMonitorOnPartitionGroupDetailForTopicApp(Subscribe subscribe, int partitionGroup) {
        checkArgument(subscribe);
        List<BrokerMonitorRecord> monitorRecords=findMonitorOnPartition(subscribe,partitionGroup);
        if(NullUtil.isEmpty(monitorRecords)) return monitorRecords;
        TopicPartitionGroup topicPartitionGroup=partitionGroupService.findByTopicAndGroup(subscribe.getNamespace().getCode() ,subscribe.getTopic().getCode(),partitionGroup);
        Set<Integer> partitionSet=new HashSet<>();
        //partitionSet.clear(); // clear cache
        String partitions=topicPartitionGroup.getPartitions();
        if(partitions!=null&&partitions.trim().length()!=0){
            List<String> partitionStrs= JSON.parseArray(partitions,String.class);
            for(String p:partitionStrs){
                if(p.trim().length()!=0){
                    partitionSet.add(Integer.valueOf(p));
                }
            }
        }
        List<BrokerMonitorRecord> retainedBrokerMonitorRecords=new ArrayList<>();
        for(BrokerMonitorRecord r:monitorRecords){
            if(partitionSet.contains(r.getPartition())){
                // brokerMonitorRecordSet.remove(r);   // remove
                r.setPartitionGroup(partitionGroup);
                retainedBrokerMonitorRecords.add(r);
            }
        }
        return retainedBrokerMonitorRecords;
    }

    /***
     *  补全partition group for partition monitor info
     **/
    public List<BrokerMonitorRecord> attachPartitionGroupInfo(Subscribe subscribe, List<BrokerMonitorRecord> monitorRecords){
        List<TopicPartitionGroup> topicPartitionGroups=partitionGroupService.findByTopic(subscribe.getNamespace(),subscribe.getTopic());
        Map<Integer/*partition*/,Integer/*partitionGroup*/>  partitionMap=new HashMap<>();
        for(TopicPartitionGroup topicPartitionGroup:topicPartitionGroups){
            for(Integer p: topicPartitionGroup.getPartitionSet()){
                partitionMap.put(p,topicPartitionGroup.getGroupNo());
            }
        }
        for(BrokerMonitorRecord m:monitorRecords){
            m.setPartitionGroup(partitionMap.get(m.getPartition()));
        }
        return monitorRecords;
    }

    @Override
    public List<BrokerMonitorRecord> findMonitorOnPartitionGroupsForTopicApp(Subscribe subscribe) {
        List<BrokerMonitorRecord> monitorRecords=new ArrayList<>();
        List<Map.Entry<PartitionGroup,Broker>> partitionGroupBroker=new ArrayList<>();
        Future<Map<String,String >> resultFuture= brokerCluster.asyncQueryOnPartitionGroup(subscribe, new RetrieveProvider<Subscribe>() {
            @Override
            public String getKey(Broker broker,PartitionGroup partitionGroup,short partition ,Subscribe condition) {
                partitionGroupBroker.add(new HashMap.SimpleEntry(partitionGroup,broker));
                return String.valueOf(partitionGroup.getGroup());
            }
            @Override
            public String getPath(String pathTemplate,PartitionGroup partitionGroup,short partition ,Subscribe condition) {
                return  String.format(pathTemplate, UrlEncoderUtil.encodeParam(CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName(),
                        subscribe.getType()==SubscribeType.PRODUCER?subscribe.getApp().getCode():CodeConverter.convertApp(subscribe.getApp(),
                                subscribe.getSubscribeGroup()),subscribe.getType().name().toLowerCase(),String.valueOf(partitionGroup.getGroup())));
            }
        },"appPartitionGroupsMonitor" ,"consumer pr producer partition groups");
        Map<String/*request key*/, String/*response*/> resultMap= brokerCluster.get(resultFuture,TIMEOUT,TimeUnit.MILLISECONDS);
        /*任一请求失败*/
        if(resultMap.size()!=partitionGroupBroker.size()) {
            logger.info("missing some of partition group on broker ,ignore!");
            //return monitorRecords;
        }
        RestResponse<ConsumerPartitionGroupMonitorInfo> restConsumePartitionGroupMonitor;
        RestResponse<ProducerPartitionGroupMonitorInfo> restProducerPartitionGroupMonitor;
        String partitionGroup;
        String partitionGroupResult;
        ConsumerPartitionGroupMonitorInfo consumePartitionGroupMonitorInfo;
        ProducerPartitionGroupMonitorInfo producerPartitionGroupMonitorInfo;
        try{
            BrokerMonitorRecord monitorRecord;
            Broker broker;
            for(Map.Entry<PartitionGroup,Broker> partitionGroupBrokerEntry:partitionGroupBroker){
                partitionGroup=String.valueOf(partitionGroupBrokerEntry.getKey().getGroup());
                broker=partitionGroupBrokerEntry.getValue();
                partitionGroupResult=resultMap.get(partitionGroup);
                if(NullUtil.isEmpty(partitionGroupResult)){
                    logger.info(String.format("ignore %s partition group %s",JSON.toJSON(subscribe),partitionGroup));
                    continue;
                }
                monitorRecord = new BrokerMonitorRecord();
                monitorRecord.setIp(broker.getIp() + ":" + broker.getPort());
                switch (subscribe.getType().value()) {
                    case CONSUMER_TYPE:
                        restConsumePartitionGroupMonitor = parse(partitionGroupResult, RestResponse.class, ConsumerPartitionGroupMonitorInfo.class, false);
                        consumePartitionGroupMonitorInfo = restConsumePartitionGroupMonitor.getData();
                        // for detail use
                        monitorRecord.setPartitionGroup(consumePartitionGroupMonitorInfo.getPartitionGroupId());
                        monitorRecord.setDeQuence(consumePartitionGroupMonitorInfo.getDeQueue());
                        monitorRecord.setPending(consumePartitionGroupMonitorInfo.getPending());
                        break;
                    case PRODUCER_TYPE:
                        restProducerPartitionGroupMonitor = parse(partitionGroupResult, RestResponse.class,ProducerPartitionGroupMonitorInfo.class, false);
                        producerPartitionGroupMonitorInfo = restProducerPartitionGroupMonitor.getData();
                        monitorRecord.setPartitionGroup(producerPartitionGroupMonitorInfo.getPartitionGroupId());
                        monitorRecord.setEnQuence(producerPartitionGroupMonitorInfo.getEnQueue());
                        break;
                }
                monitorRecords.add(monitorRecord);
            }
        }catch (Exception e){
            logger.info("broker asyncQueryOnBroker occurs parse exception.", e);
            throw new ServiceException(INTERNAL_SERVER_ERROR,e.getMessage(), e);
        }
        return  monitorRecords;
    }

    /**
     * joyqueue client producer may not have enquence
     *
     **/
    @Override
    public List<BrokerMonitorRecord> findMonitorOnPartitionGroups(Subscribe subscribe) {
        checkArgument(subscribe);
        List<BrokerMonitorRecord> monitorRecords=new ArrayList<>();
        List<Broker> brokers=new ArrayList<>();
        Future<Map<String,String >> resultFuture= brokerCluster.asyncQueryOnBroker(subscribe, new RetrieveProvider<Subscribe>() {
            @Override
            public String getKey(Broker broker,PartitionGroup partitionGroup, short partition , Subscribe condition) {
                brokers.add(broker);
                return broker.getIp()+":"+broker.getPort();
            }
            @Override
            public String getPath(String pathTemplate,PartitionGroup partitionGroup,short partition ,Subscribe condition) {
                return  String.format(pathTemplate,UrlEncoderUtil.encodeParam( CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName(),
                        subscribe.getType()==SubscribeType.PRODUCER?subscribe.getApp().getCode():CodeConverter.convertApp(subscribe.getApp(),
                                subscribe.getSubscribeGroup())));
            }
        },"topicPartitionGroupsMonitor" ,"topic partition groups ");
        Map<String/*request key*/, String/*response*/> resultMap= brokerCluster.get(resultFuture,TIMEOUT,TimeUnit.MILLISECONDS);
        /*任一请求失败*/
        if(resultMap.size()!=brokers.size()) {
            logger.info("missing some of partition group on broker ,ignore!");
        }
        BrokerMonitorRecord record;
        RestResponse<List<PartitionGroupMonitorInfo>> restPartitionGroupsMonitor;
        try {
            String r;
            String ipWithPort;
            for(Broker b:brokers){
                ipWithPort=b.getIp()+":"+b.getPort();
                r= resultMap.get(ipWithPort);
                if(NullUtil.isEmpty(r)){
                    logger.info(String.format("ignore %s partition group on broker %s",JSON.toJSON(subscribe),b.getIp()));
                    continue;
                }
                restPartitionGroupsMonitor=parse(r,RestResponse.class,PartitionGroupMonitorInfo.class,true);
                List<PartitionGroupMonitorInfo> producerPartitionGroupMonitors= restPartitionGroupsMonitor.getData();
                for(PartitionGroupMonitorInfo p:producerPartitionGroupMonitors) {
                    record= new BrokerMonitorRecord();
                    record.setPartitionGroup(p.getPartitionGroup());
                    record.setIp(ipWithPort);
                    record.setEnQuence(p.getEnQueue());
                    record.setDeQuence(p.getDeQueue());
                    monitorRecords.add(record);
                }
            }
        }catch (Exception e){
            logger.info("broker asyncQueryOnBroker occurs parse exception.", e);
            throw new ServiceException(INTERNAL_SERVER_ERROR,e.getMessage());
        }
        return monitorRecords;
    }


    @Override
    public List<ConnectionMonitorInfoWithIp> findConnectionOnBroker(Subscribe subscribe) {
        checkArgument(subscribe);
        List<ConnectionMonitorInfoWithIp> connectionRecords=new ArrayList<>();
        List<Broker> brokers=new ArrayList<>();
        Future<Map<String,String >> resultFuture= brokerCluster.asyncQueryOnBroker(subscribe, new RetrieveProvider<Subscribe>() {
            @Override
            public String getKey(Broker broker, PartitionGroup partitionGroup,short partition , Subscribe condition) {
                brokers.add(broker);
                return broker.getIp()+":"+broker.getPort();
            }
            @Override
            public String getPath(String pathTemplate,PartitionGroup partitionGroup,short partition ,Subscribe condition) {
                return  String.format(pathTemplate, UrlEncoderUtil.encodeParam(CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName(),
                        subscribe.getType()==SubscribeType.PRODUCER?subscribe.getApp().getCode():CodeConverter.convertApp(subscribe.getApp(),
                                subscribe.getSubscribeGroup())));
            }
        },"appConnection" ,"app connection on broker");
        Map<String/*request key*/, String/*response*/> resultMap= brokerCluster.get(resultFuture,TIMEOUT,TimeUnit.MILLISECONDS);
        RestResponse<ConnectionMonitorInfo> restAppConnectionMonitor;
        try {
            String r;
            String hostWithPort;
            ConnectionMonitorInfoWithIp connectionMonitorInfoWithIp;
            ConnectionMonitorInfo con;
            for(Broker b:brokers){
                hostWithPort=b.getIp()+":"+b.getPort();
                r= resultMap.get(hostWithPort);
                if(NullUtil.isEmpty(r)){
                    logger.info(String.format("ignore %s broker %s connection",JSON.toJSON(subscribe),b.getIp()));
                    continue;
                }
                restAppConnectionMonitor = parse(r, RestResponse.class, ConnectionMonitorInfo.class,false);
                con=restAppConnectionMonitor.getData();
                connectionMonitorInfoWithIp=new ConnectionMonitorInfoWithIp();
                connectionMonitorInfoWithIp.setIp(hostWithPort);
                connectionMonitorInfoWithIp.setConsumer(con.getConsumer());
                connectionMonitorInfoWithIp.setProducer(con.getProducer());
                connectionMonitorInfoWithIp.setTotal(con.getTotal());
                connectionMonitorInfoWithIp.setId(b.getId());
                connectionRecords.add(connectionMonitorInfoWithIp);
            }
            //Future
        }catch (Exception e){
            logger.info("broker asyncQueryOnBroker occurs parse exception.", e);
            throw new ServiceException(INTERNAL_SERVER_ERROR,e.getMessage());
        }
        return connectionRecords;
    }

    public List<PartitionGroupPosition> findPartitionGroupMetric(String namespace, String topic, Integer groupNo){
        TopicPartitionGroup topicPartitionGroup = partitionGroupService.findByTopicAndGroup(namespace,topic,groupNo);
        String path = "partitiongroupIndex";
        Future<Map<String,String >> resultFuture = null;
        try {
            resultFuture = brokerCluster.asyncQueryAllBroker(namespace,topic,groupNo,path,path);
        } catch (Exception e) {
            logger.error("asynQuery,error",e);
        }
        Map<String/*request key*/, String/*response*/> resultMap= brokerCluster.get(resultFuture,TIMEOUT,TimeUnit.MILLISECONDS);
        Map<String,PartitionGroupMetric> map = new HashMap();
        resultMap.forEach( (k,v) -> {
            RestResponse<PartitionGroupMetric>  restResponse = parse(v,RestResponse.class, PartitionGroupMetric.class,false);
            map.put(k,restResponse.getData());
        });
        String requestKey = String.valueOf(topicPartitionGroup.getLeader())+"_"+groupNo;
        return getPartitionGroupInterval(requestKey,map);
    }



    @Override
    public ArchiveMonitorInfo findArchiveState(String ip,int  port) {
        RestResponse<ArchiveMonitorInfo> archiveMonitorRest;
        try{
            String url = String.format(urlMappingService.urlTemplate("archiveMonitor"), ip, String.valueOf(port));
            String body= HttpUtil.get(url);
            archiveMonitorRest= parse(body,RestResponse.class,ArchiveMonitorInfo.class,false );
            return archiveMonitorRest.getData();
        }catch (Exception e){
            logger.error("archive monitor",e);
            throw new ServiceException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 查询Broker上的元数据
     * @param broker
     * @param group 分组
     * @param topicFullName
     * @return
     */
    @Override
    public BrokerMetadata findBrokerMetadata(Broker broker, String topicFullName, int group) {
        String ip = broker.getIp();
        int port = broker.getMonitorPort();
        Integer id = new Integer((int) broker.getId());
        Preconditions.checkArgument(StringUtils.isNotEmpty(ip) && port > 0 && id > 0
                && StringUtils.isNotEmpty(topicFullName), "query broker metadata params incorrect.");

        RestResponse<JSONObject> response;
        try{
            String url = String.format(urlMappingService.urlTemplate("topicPartitionGroupMetadata"), ip, String.valueOf(port), topicFullName);
            String body = HttpUtil.get(url);
            response = parse(body, RestResponse.class, JSONObject.class,false);
            if (response.getCode() != 200) {
                String msg = String.format("query broker %s metadata under topic %s error. response code %s", ip + ":" + port, topicFullName, response.getCode());
                logger.error(msg);
                throw new ServiceException(NOT_FOUND, msg);
            }
            JSONObject data = response.getData();
            try {
                JSONObject pgObj = (JSONObject) ((JSONObject) data.get("partitionGroups")).get(group);
                JSONObject leaderObj = (JSONObject) pgObj.get("leaderBroker");
                JSONObject obj = (JSONObject) ((JSONObject) pgObj.get("brokers")).get(id);
                obj.put("leaderAddress", leaderObj.get("address"));
                obj.put("leaderBrokerId", leaderObj.get("id"));
                obj.put("leaderRetryType", leaderObj.get("retryType"));
                obj.put("leaderPermission", leaderObj.get("permission"));
                obj.put("leaderIp", leaderObj.get("ip"));
                obj.put("leaderPort", leaderObj.get("port"));
                return JSONObject.toJavaObject(obj, BrokerMetadata.class);
            } catch (Exception e) {
                String msg = String.format("transform the response data of the broker %s metadata under topic %s error.", ip + ":" + port, topicFullName);
                logger.error(msg, e);
                throw new ServiceException(INTERNAL_SERVER_ERROR, msg, e);
            }
        }catch (Exception e){
            logger.error("query broker metadata error.", e);
            throw new ServiceException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    //计算位移间隔
    private List<PartitionGroupPosition> getPartitionGroupInterval(String leaderBroker,Map<String,PartitionGroupMetric> partitionGroupMetricMap){
        PartitionGroupMetric masterMetric=partitionGroupMetricMap.get(leaderBroker);
        List<PartitionGroupPosition> positionList = new ArrayList<>();
        partitionGroupMetricMap.forEach((k,v) ->{
            PartitionGroupPosition partitionGroupPosition = positionConvert(masterMetric,v);
            if (leaderBroker.equals(k)) {
                partitionGroupPosition.setLeader(true);
            }
            partitionGroupPosition.setBrokerId(k);
            positionList.add(partitionGroupPosition);
            logger.info("getPartitionGroupInterval brokerid:{},partitionGroupPosition:{}",k,JSON.toJSONString(partitionGroupPosition));
        });
        return positionList;
    }

    //格式转换并计算位移差
    private PartitionGroupPosition positionConvert(PartitionGroupMetric master,PartitionGroupMetric slave) {
        PartitionGroupPosition partitionGroupPosition = new PartitionGroupPosition();
        partitionGroupPosition.setRightPosition(slave.getRightPosition());
        partitionGroupPosition.setPartitionGroup(slave.getPartitionGroup());
        partitionGroupPosition.setRightPositionInterval(master.getRightPosition()-slave.getRightPosition());
        List<PartitionPosition> partitionPositions = new ArrayList<>();
        for(int i=0;i<master.getPartitionMetrics().length;i++ ){
            PartitionMetric partitionMetric1 = master.getPartitionMetrics()[i];
            PartitionMetric partitionMetric2 = slave.getPartitionMetrics()[i];
            PartitionPosition partitionPosition = new PartitionPosition();
            partitionPosition.setPartition(partitionMetric2.getPartition());
            partitionPosition.setRightPosition(partitionMetric2.getRightIndex());
            partitionPosition.setRightPositionInterval(partitionMetric1.getRightIndex()-partitionMetric2.getRightIndex());
            partitionPositions.add(partitionPosition);
        }
        partitionGroupPosition.setPartitionPositionList(partitionPositions);
        return partitionGroupPosition;
    }

    /**
     *
     * add @param from to @param to
     *
     */
    public RetryMonitorInfo add(RetryMonitorInfo to,RetryMonitorInfo from){
        if(!NullUtil.isEmpty(to)&&!NullUtil.isEmpty(from)) {
            to.setSuccess(to.getSuccess() + from.getSuccess());
            to.setCount(to.getCount() + from.getCount());
            to.setFailure(to.getFailure() + from.getFailure());
        }else {

            return NullUtil.isEmpty(to)?from:to;
        }
        return to;
    }


    public PendingMonitorInfo add(PendingMonitorInfo to,PendingMonitorInfo from){
        if(!NullUtil.isEmpty(to)&&!NullUtil.isEmpty(from)) {
            to.setCount(to.getCount()+from.getCount());
        }else{
            return NullUtil.isEmpty(to)?from:to;
        }
        return to;
    }


    public DeQueueMonitorInfo add(DeQueueMonitorInfo to,DeQueueMonitorInfo from){
        if(!NullUtil.isEmpty(to)&&!NullUtil.isEmpty(from)) {
            to.setCount(to.getCount() + from.getCount());
            to.setMax(to.getMax() + from.getMax());
            to.setSize(to.getSize() + from.getSize());
            to.setOneMinuteRate(to.getOneMinuteRate() + from.getOneMinuteRate());
            to.setTotalSize(to.getTotalSize() + from.getTotalSize());
            to.setTp90(Math.max(to.getTp90(), from.getTp90()));
            to.setTp99(Math.max(to.getTp99(), from.getTp99()));
        }else {
            return NullUtil.isEmpty(to)?from:to;
        }
        return to;
    }

    public EnQueueMonitorInfo add(EnQueueMonitorInfo to,EnQueueMonitorInfo from){
        if(!NullUtil.isEmpty(to)&&!NullUtil.isEmpty(from)) {
            to.setCount(to.getCount() + from.getCount());
            to.setMax(to.getMax() + from.getMax());
            to.setSize(to.getSize() + from.getSize());
            to.setOneMinuteRate(to.getOneMinuteRate() + from.getOneMinuteRate());
            to.setTotalSize(to.getTotalSize() + from.getTotalSize());
            to.setTp90(Math.max(to.getTp90(), from.getTp90()));
            to.setTp99(Math.max(to.getTp99(), from.getTp99()));
        }else{
            return NullUtil.isEmpty(to)?from:to;
        }
        return to;
    }



    /**
     * merge monitor record
     **/
    public BrokerMonitorRecord add(BrokerMonitorRecord to, BrokerMonitorRecord from){
        if(!NullUtil.isEmpty(to)&&!NullUtil.isEmpty(from)) {
            to.setPending(add(to.getPending(),from.getPending()));
            to.setRetry(add(to.getRetry(),from.getRetry()));
            to.setEnQuence(add(to.getEnQuence(),from.getEnQuence()));
            to.setDeQuence(add(to.getDeQuence(),from.getDeQuence()));
        }else{
            return NullUtil.isEmpty(to)?from:to;
        }
        return  to;
    }



    public List<BrokerMonitorRecord> transferConsumerPartition(List<ConsumerPartitionMonitorInfo> consumerPartitionMonitors,String ipPort){
        BrokerMonitorRecord record;
        List<BrokerMonitorRecord> monitorRecords=new ArrayList<>();
        if(NullUtil.isEmpty(consumerPartitionMonitors)) return monitorRecords;
        for(ConsumerPartitionMonitorInfo p:consumerPartitionMonitors) {
            record= new BrokerMonitorRecord();
            record.setIp(ipPort);
            record.setPartition(p.getPartition());
            record.setDeQuence(p.getDeQueue());
            record.setPending(p.getPending());
            monitorRecords.add(record);
        }
        return monitorRecords;
    }

    public List<BrokerMonitorRecord> transferProducerPartition(List<ProducerPartitionMonitorInfo> producerPartitionMonitorInfos,String ipPort){
        BrokerMonitorRecord record;
        List<BrokerMonitorRecord> monitorRecords=new ArrayList<>();
        if(NullUtil.isEmpty(producerPartitionMonitorInfos)) return monitorRecords;
        for(ProducerPartitionMonitorInfo p:producerPartitionMonitorInfos) {
            record= new BrokerMonitorRecord();
            record.setPartition(p.getPartition());
            record.setIp(ipPort);
            record.setEnQuence(p.getEnQueue());
            monitorRecords.add(record);
        }
        return monitorRecords;
    }



    /**
     *
     * @return  -1 to indicate incomplete
     **/
    public BrokerMonitorRecord fillIncompleteBrokerMonitor(){
        BrokerMonitorRecord record=new BrokerMonitorRecord();
        record.setConnections(-1);
        record.setDeQuence(new DeQueueMonitorInfo());
        record.getDeQuence().setCount(-1);
        record.setEnQuence(new EnQueueMonitorInfo());
        record.getEnQuence().setCount(-1);
        record.setPending(new PendingMonitorInfo());
        record.getPending().setCount(-1);
        record.setRetry(new RetryMonitorInfo());
        record.getRetry().setCount(-1);
        return record;
    }

    /**
     * validate subscribe param
     * @param subscribe
     */
    private void checkArgument(Subscribe subscribe) {
        Preconditions.checkArgument(subscribe != null, "topic field in subscribe arg can not be null.");
        Preconditions.checkArgument(subscribe.getTopic() != null, "topic field in subscribe arg can not be null.");
        Preconditions.checkArgument(subscribe.getApp() != null, "app field in subscribe arg can not be null.");
        Preconditions.checkArgument(subscribe.getType() != null, "subscribeGroup field in subscribe arg can not be null.");
    }

}
