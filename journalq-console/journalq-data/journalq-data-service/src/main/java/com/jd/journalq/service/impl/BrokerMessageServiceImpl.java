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


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jd.journalq.async.BrokerMonitorClusterQuery;
import com.jd.journalq.async.BrokerClusterQuery;
import com.jd.journalq.async.RetrieveProvider;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.monitor.BrokerMessageInfo;
import com.jd.journalq.monitor.RestResponse;
import com.jd.journalq.monitor.RestResponseCode;
import com.jd.journalq.convert.CodeConverter;
import com.jd.journalq.model.domain.Subscribe;
import com.jd.journalq.model.domain.SubscribeType;
import com.jd.journalq.model.domain.Broker;
import com.jd.journalq.model.domain.SimplifiedBrokeMessage;
import com.jd.journalq.other.HttpRestService;
import com.jd.journalq.service.BrokerMessageService;
import com.jd.journalq.service.LeaderService;
import com.jd.journalq.util.NullUtil;
import com.jd.journalq.util.UrlEncoderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


@Service("brokerMessageService")
public class BrokerMessageServiceImpl implements BrokerMessageService {

    private final static Logger logger= LoggerFactory.getLogger(BrokerMessageServiceImpl.class);
    private final static long TIMEOUT=10000;
    public final static ObjectMapper mapper = new ObjectMapper();
    @Resource(type = BrokerMonitorClusterQuery.class)
    private BrokerClusterQuery<Subscribe> brokerClusterQuery;
    @Autowired
    private LeaderService leaderService;
    @Autowired(required = false)
    private HttpRestService httpRestService;

    @Override
    public List<SimplifiedBrokeMessage> previewPendingMessage(Subscribe subscribe, int count) {
        List<SimplifiedBrokeMessage> simplifiedBrokeMessages=new ArrayList<>();
        List<Broker> brokers=new ArrayList<>();
        Future<Map<String,String >> resultFuture= brokerClusterQuery.asyncQueryOnBroker(subscribe, new RetrieveProvider<Subscribe>() {
            @Override
            public String getKey(Broker broker, PartitionGroup partitionGroup,short partition ,Subscribe condition) {
                brokers.add(broker);
                return broker.getIp()+":"+broker.getPort();
            }
            @Override
            public String getPath(String pathTemplate,PartitionGroup partitionGroup,short partition,Subscribe condition) {
                return  String.format(pathTemplate, UrlEncoderUtil.encodeParam(CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName(),
                        subscribe.getType()== SubscribeType.PRODUCER?subscribe.getApp().getCode():CodeConverter.convertApp(subscribe.getApp(),
                                subscribe.getSubscribeGroup()),String.valueOf(count)));
            }
        },"pendingByteMessage" ," pending message");
        Map<String/*request key*/, String/*response*/> resultMap= brokerClusterQuery.get(resultFuture,TIMEOUT,TimeUnit.MILLISECONDS);
        SimplifiedBrokeMessage message;
        RestResponse<List<BrokerMessageInfo>> brokerMessageResponse;
        JavaType messagesListType=mapper.getTypeFactory().constructParametricType(List.class, BrokerMessageInfo.class);
        JavaType restListBrokerMessageType=mapper.getTypeFactory().constructParametricType(RestResponse.class, messagesListType);
        try {
            for (Map.Entry<String, String> response : resultMap.entrySet()) {
                brokerMessageResponse = mapper.readValue(response.getValue(), restListBrokerMessageType);
                if (brokerMessageResponse.getCode()== RestResponseCode.SUCCESS.getCode()) {
                    if(!NullUtil.isEmpty(brokerMessageResponse.getData())){
                        for (BrokerMessageInfo m : brokerMessageResponse.getData()) {
                            message = BrokerMessageConvert(m);
                            simplifiedBrokeMessages.add(message);
                        }
                    }else{
                        logger.info(String.format("%s preview message request success,but empty message",response.getKey()));
                    }
                }else{
                    logger.info(String.format("%s preview message request failed",response.getKey()));
                }
            }
        }catch (Exception e){
            logger.info("parse broker message error",e);
        }
        return simplifiedBrokeMessages;
    }

    @Override
    public List<SimplifiedBrokeMessage> previewNewestMessage(long topicId, String topic,String app, int count) {

        return null;
    }

    @Override
    public List<BrokerMessageInfo> viewMessage(Subscribe subscribe,String partition, String index, int count) {
        Map.Entry<PartitionGroup, Broker> brokerEntry = leaderService.findPartitionLeaderBrokerDetail(subscribe.getNamespace().getCode(),subscribe.getTopic().getCode(),Integer.valueOf(partition));
        Broker broker = brokerEntry.getValue();
        String path="getPartitionMessageByIndex";
        String[] args=new String[7];
        args[0]=broker.getIp();
        args[1]=String.valueOf(broker.getMonitorPort());
        args[2]=CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName();
        args[3]=subscribe.getApp().getCode();
        args[4]=partition;
        args[5]=index;
        args[6]=String.valueOf(count);
        RestResponse<List<BrokerMessageInfo>> restResponse = httpRestService.get(path,BrokerMessageInfo.class,true,args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return null;
    }

    @Override
    public Long getPartitionIndexByTime(Subscribe subscribe,String partition, String timestamp) {
        Map.Entry<PartitionGroup, Broker> brokerEntry = leaderService.findPartitionLeaderBrokerDetail(subscribe.getNamespace().getCode(),subscribe.getTopic().getCode(),Integer.valueOf(partition));
        Broker broker = brokerEntry.getValue();
        String path="getTopicAppPartitionIndexByTime";
        String[] args=new String[6];
        args[0]=broker.getIp();
        args[1]=String.valueOf(broker.getMonitorPort());
        args[2]=CodeConverter.convertTopic(subscribe.getNamespace(),subscribe.getTopic()).getFullName();
        args[3]=subscribe.getApp().getCode();
        args[4]=partition;
        args[5]=timestamp;
        RestResponse<Long> restResponse = httpRestService.get(path,Long.class,false,args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return null;
    }


    @Override
    public SimplifiedBrokeMessage download(String ip, int port, String topic, String app, short partition, long index) {
        return null;
    }


    private SimplifiedBrokeMessage BrokerMessageConvert(BrokerMessageInfo m) {
        SimplifiedBrokeMessage message = new SimplifiedBrokeMessage();
//        message.setQueryId(response.getKey());
        message.setId(m.getPartition() + "-" + m.getMsgIndexNo());
        message.setSendTime(m.getStartTime());
        message.setStoreTime(m.getStoreTime());
        message.setBusinessId(m.getBusinessId());
        message.setBody(new String(m.getBody().getBytes(Charset.forName("utf-8"))));
        message.setAttributes(m.getAttributes());
        message.setFlag(false);
        return message;
    }
}
