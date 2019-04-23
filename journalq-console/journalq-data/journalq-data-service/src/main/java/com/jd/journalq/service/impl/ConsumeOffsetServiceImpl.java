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
package com.jd.journalq.service.impl;

import com.alibaba.fastjson.JSON;
import com.jd.journalq.async.BrokerClusterQuery;
import com.jd.journalq.async.BrokerMonitorClusterQuery;
import com.jd.journalq.async.DefaultBrokerInfoFuture;
import com.jd.journalq.async.RetrieveProvider;
import com.jd.journalq.async.UpdateProvider;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.monitor.PartitionAckMonitorInfo;
import com.jd.journalq.monitor.PartitionLeaderAckMonitorInfo;
import com.jd.journalq.monitor.RestResponse;
import com.jd.journalq.convert.CodeConverter;
import com.jd.journalq.model.domain.Broker;
import com.jd.journalq.model.domain.PartitionOffset;
import com.jd.journalq.model.domain.Subscribe;
import com.jd.journalq.other.HttpRestService;
import com.jd.journalq.service.BrokerRestUrlMappingService;
import com.jd.journalq.service.ConsumeOffsetService;
import com.jd.journalq.service.LeaderService;
import com.jd.journalq.service.TopicPartitionGroupService;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.util.AsyncHttpClient;
import com.jd.journalq.util.JSONParser;
import com.jd.journalq.util.NullUtil;
import com.jd.journalq.util.UrlEncoderUtil;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * offset management
 * @author  wangjin18
 *
 **/
@Service("consumeOffsetService")
//todo 待移走
public class ConsumeOffsetServiceImpl implements ConsumeOffsetService {
    private static final Logger logger= LoggerFactory.getLogger(ConsumeOffsetServiceImpl.class);
    private static final long TIMEOUT=60000;

    @Resource(type = BrokerMonitorClusterQuery.class)
    private BrokerClusterQuery<Subscribe> brokerCluster;
    @Autowired
    private TopicPartitionGroupService partitionGroupService;

    @Autowired
    private BrokerRestUrlMappingService urlMappingService;

    @Autowired
    private LeaderService leaderService;


    @Autowired
    private HttpRestService httpRestService;

    @Override
    public List<PartitionLeaderAckMonitorInfo> offsets(Subscribe subscribe) {
        this.checkArgument(subscribe);
        List<Broker> brokers=new ArrayList<>();
        List<PartitionLeaderAckMonitorInfo> partitionAckMonitorInfos=new ArrayList<>();
        Future<Map<String,String >> resultFuture=brokerCluster.asyncQueryOnBroker(subscribe, new RetrieveProvider<Subscribe>() {
            @Override
            public String getKey(Broker broker, PartitionGroup partitionGroup,short partition,Subscribe condition) {
                brokers.add(broker);
                return broker.getIp()+":"+broker.getPort();
            }

            @Override
            public String getPath(String pathTemplate, PartitionGroup partitionGroup,short partition ,Subscribe condition) {
                return String.format(pathTemplate, UrlEncoderUtil.encodeParam(CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName(),
                       CodeConverter.convertApp(subscribe.getApp(),subscribe.getSubscribeGroup())));
            }
        },"appConsumeOffsetMonitor","appConsumeOffsetMonitor");
        Map<String/*request key*/, String/*response*/> resultMap= brokerCluster.get(resultFuture,TIMEOUT, TimeUnit.MILLISECONDS);
        if(resultMap.size()!=brokers.size()) {
            logger.info("missing broker partition consume offset,ignore");
            //return partitionAckMonitorInfos; //some request failed fail
        }
        return tagLeaderPartitionOffset(resultMap,subscribe);
    }


    /**
     *
     *  tag for leader partition consume offset
     *
     **/
    public List<PartitionLeaderAckMonitorInfo> tagLeaderPartitionOffset(Map<String,String> brokerPartitionOffsets,Subscribe subscribe){
        List<PartitionLeaderAckMonitorInfo> partitionAckMonitorInfos=new ArrayList<>();
        Map<Short,Broker> partitionBrokers=leaderService.findPartitionLeaderBrokerDetail(subscribe.getTopic().getCode(),subscribe.getNamespace().getCode());
        RestResponse<List<PartitionAckMonitorInfo>> restPartitionAckMonitorResponse;
        for(Map.Entry<String,String>   brokerPartitionOffset:brokerPartitionOffsets.entrySet()){
            restPartitionAckMonitorResponse=JSONParser.parse(brokerPartitionOffset.getValue(),RestResponse.class,PartitionAckMonitorInfo.class,true);
            partitionAckMonitorInfos.addAll(tagLeaderPartitionOffset(brokerPartitionOffset.getKey(),restPartitionAckMonitorResponse.getData(),partitionBrokers));
        }
        return partitionAckMonitorInfos;
    }


    /**
     *
     *
     * @param broker ip:port
     *
     **/

    public List<PartitionLeaderAckMonitorInfo> tagLeaderPartitionOffset(String broker,List<PartitionAckMonitorInfo> partitionAckMonitorInfos,Map<Short,Broker> partitionBrokers){
        PartitionLeaderAckMonitorInfo partitionLeaderAckMonitorInfo;
        List<PartitionLeaderAckMonitorInfo> partitionLeaderAckMonitorInfos=new ArrayList<>();
        for(PartitionAckMonitorInfo p:partitionAckMonitorInfos){
            partitionLeaderAckMonitorInfo=new PartitionLeaderAckMonitorInfo(p,false);
            Broker b=partitionBrokers.get(p.getPartition());
            if(!NullUtil.isEmpty(b)) {
                String partitionLeaderBrokerKey = b.getIp() + ":" + b.getPort();
                if (partitionLeaderBrokerKey.equals(broker)) {
                    partitionLeaderAckMonitorInfo.setLeader(true);
                }
                partitionLeaderAckMonitorInfos.add(partitionLeaderAckMonitorInfo);
            }
        }
        return  partitionLeaderAckMonitorInfos;
    }



    @Override
    public long offset(Subscribe subscribe, short partition) {
        checkArgument(subscribe);
        Map.Entry<PartitionGroup,Broker> partitionGroupBroker=leaderService.findPartitionLeaderBrokerDetail(subscribe.getNamespace().getCode(),subscribe.getTopic().getCode(),partition);
        if(!NullUtil.isEmpty(partitionGroupBroker)){
            Broker b=partitionGroupBroker.getValue();
            String[] args=new String[3];
            args[0]=b.getIp();
            args[1]=String.valueOf(b.getMonitorPort());
            args[2]=String.valueOf(partitionGroupBroker.getKey().getGroup());
            String pathKey="appPartitionOffsetMonitor";
            RestResponse<Long> restResponse=httpRestService.get(pathKey,Long.class,false,args);
            return  restResponse.getData();
        }else{
            throw new IllegalArgumentException("partition group or leader broker not found");
        }
    }

    @Override
    public List<PartitionAckMonitorInfo> timeOffset(Subscribe subscribe, long timeMs) {
        List<Broker> brokerList =leaderService.findLeaderBroker(subscribe.getTopic().getCode(),subscribe.getNamespace().getCode());
        if(!NullUtil.isEmpty(brokerList)){
            List<PartitionAckMonitorInfo> partitionAckMonitorInfos = new ArrayList<>();
            Map<Short,Broker> partitionBrokers=leaderService.findPartitionLeaderBrokerDetail(subscribe.getTopic().getCode(),subscribe.getNamespace().getCode());
            for (Broker broker:brokerList) {
                String[] args = new String[5];
                args[0] = broker.getIp();
                args[1] = String.valueOf(broker.getMonitorPort());
                args[2] = CodeConverter.convertTopic(subscribe.getNamespace(), subscribe.getTopic()).getFullName();
                args[3] = CodeConverter.convertApp(subscribe.getApp(), subscribe.getSubscribeGroup());
                args[4] = String.valueOf(timeMs);
                String pathKey = "getTopicAppOffset";
                RestResponse<List<PartitionAckMonitorInfo>> restResponse = httpRestService.get(pathKey, PartitionAckMonitorInfo.class, true, args);
                if (restResponse.getData() != null) {
                   List<PartitionLeaderAckMonitorInfo>  partitionLeaderAckMonitorInfos=tagLeaderPartitionOffset(broker.getIp()+":"+broker.getPort(),restResponse.getData(),partitionBrokers);
                   for(PartitionLeaderAckMonitorInfo leaderAckMonitorInfo:partitionLeaderAckMonitorInfos) {
                       if(leaderAckMonitorInfo.isLeader())
                            partitionAckMonitorInfos.add(leaderAckMonitorInfo);
                   }
                }
            }
            return  partitionAckMonitorInfos;
        }else{
            throw new IllegalArgumentException("partition group or leader broker not found");
        }
    }

    @Override
    public boolean resetOffset(Subscribe subscribe, short partition, long offset) {
        Map.Entry<PartitionGroup,Broker> partitionGroupBroker=leaderService.findPartitionLeaderBrokerDetail(subscribe.getNamespace().getCode(),subscribe.getTopic().getCode(),partition);
        if(!NullUtil.isEmpty(partitionGroupBroker)){
            Broker b=partitionGroupBroker.getValue();
            String[] args=new String[6];
            args[0]=b.getIp();
            args[1]=String.valueOf(b.getMonitorPort());
            args[2]=CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName();
            args[3]=CodeConverter.convertApp(subscribe.getApp(),subscribe.getSubscribeGroup());
            args[4]=String.valueOf(partition);
            args[5]=String.valueOf(offset);
            String pathKey="resetAppPartitionOffset";
            RestResponse<Boolean> restResponse=httpRestService.put(pathKey,Boolean.class,false, JSON.toJSONString(args),args);
            return  restResponse.getData();
        }else{
            throw new IllegalArgumentException("partition group or leader broker not found");
        }
    }

    @Override
    public boolean resetOffset(Subscribe subscribe, long timeMs) {
        List<Broker> brokers=new ArrayList<>();
        String topic=CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName();
        String app=CodeConverter.convertApp(subscribe.getApp(),subscribe.getSubscribeGroup());
        Future<Map<String,String>> resultFuture=brokerCluster.asyncUpdateOnBroker(subscribe, new UpdateProvider<Subscribe>() {
            @Override
            public String getPath(String pathTemplate, PartitionGroup partitionGroup, short partition, Subscribe condition) {
                return String.format(pathTemplate,UrlEncoderUtil.encodeParam(topic,app,String.valueOf(timeMs)));
            }
            @Override
            public HttpUriRequest getRequest(String uri, PartitionGroup partitionGroup, short partition, Subscribe condition) {
                HttpPut put=new HttpPut(uri);
                try {
                    put.setEntity(new StringEntity(String.valueOf(partition)));
                }catch (UnsupportedEncodingException e){
                    logger.info("unsupported",e);
                }
                return put;
            }
            @Override
            public String getKey(Broker broker, PartitionGroup partitionGroup,short partition, Subscribe condition) {
                brokers.add(broker);
                return broker.getIp()+":"+broker.getPort();
            }
        },"resetAppTopicOffsetByTime","reset consume topic Offset by time");
        Map<String/*request key*/, String/*response*/> resultMap= brokerCluster.get(resultFuture,TIMEOUT, TimeUnit.MILLISECONDS);
        if(resultMap.size()==brokers.size()){
            for(String content:resultMap.values()){
                RestResponse<Boolean>  restResponse= JSONParser.parse(content,RestResponse.class,Boolean.class,false);
                if(restResponse.getData()==false) {
                   logger.info("reset by time failed,{}",restResponse.getMessage());
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean resetOffset(Subscribe subscribe, List<PartitionOffset> offsets) {
        checkArgument(subscribe);
        List<Map.Entry<PartitionGroup,Broker>> partitionGroupBrokers=leaderService.findPartitionGroupLeaderBrokerDetail(subscribe.getTopic().getCode(),subscribe.getNamespace().getCode());
        Map<Short,Map.Entry<PartitionGroup,Broker>>  partitionBroker=partitionBroker(partitionGroupBrokers);
        String pathTemplate= urlMappingService.pathTemplate("resetAppPartitionOffset");
        CountDownLatch latch = new CountDownLatch(offsets.size());
        Map<String/*request key*/,String/*response*/>  resultMap=new ConcurrentHashMap<>(offsets.size());
        String url;
        String path;
        String topic=CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName();
        String app=CodeConverter.convertApp(subscribe.getApp(),subscribe.getSubscribeGroup());
        Map.Entry<PartitionGroup,Broker> partitionGroupBroker;
        int request=0;
        for(PartitionOffset  offset: offsets){
            //monitorUrl+ path with parameter
            partitionGroupBroker=partitionBroker.get(offset.getPartition());
            if(!NullUtil.isEmpty(partitionGroupBroker)) {
                path=String.format(pathTemplate,UrlEncoderUtil.encodeParam(topic,app,String.valueOf(offset.getPartition()),String.valueOf(offset.getOffset())));
                url = urlMappingService.monitorUrl(partitionGroupBroker.getValue()) + path;
                logger.info(String.format("start sync request,%s", url));
                HttpPut put=new HttpPut(url);
                try {
                    put.setEntity(new StringEntity(String.valueOf(offset.getOffset())));
                }catch (UnsupportedEncodingException e){
                    throw new IllegalStateException(e);
                }
                AsyncHttpClient.AsyncRequest(put, new AsyncHttpClient.ConcurrentHttpResponseHandler(url,System.currentTimeMillis(),latch,String.valueOf(offset.getPartition()),resultMap));
                request++;
            }else{
                logger.info("partition group broker not found!");
            }
        }
        brokerCluster.get(new DefaultBrokerInfoFuture(latch,resultMap,"reset offset"),TIMEOUT,TimeUnit.MILLISECONDS);
        if(resultMap.size()==request){
           for(String content:resultMap.values()){
            RestResponse<Boolean>  restResponse=   JSONParser.parse(content,RestResponse.class,Boolean.class,false);
            if(restResponse.getData()==false) {
                logger.info("reset failed,{}",restResponse.getMessage());
                return false;
            }
           }
           return true;
        }
        return false;
    }

    private  Map<Short,Map.Entry<PartitionGroup,Broker>> partitionBroker(List<Map.Entry<PartitionGroup,Broker>> partitionGroupBrokers){
        Map<Short,Map.Entry<PartitionGroup,Broker>> partitionBroker=new HashMap<>();
        for(Map.Entry<PartitionGroup,Broker> e:partitionGroupBrokers){
          Set<Short> partitions=e.getKey().getPartitions();
          for(Short p:partitions){
              partitionBroker.put(p,e);
          }
        }
        return partitionBroker;
    }

    /**
     * validate subscribe param
     * @param subscribe
     */
    private void checkArgument(Subscribe subscribe) {
        Preconditions.checkArgument(subscribe != null, "topic field in subscribe arg can not be null.");
        Preconditions.checkArgument(subscribe.getTopic() != null, "topic field in subscribe arg can not be null.");
        Preconditions.checkArgument(subscribe.getApp() != null, "app field in subscribe arg can not be null.");
    }


}
