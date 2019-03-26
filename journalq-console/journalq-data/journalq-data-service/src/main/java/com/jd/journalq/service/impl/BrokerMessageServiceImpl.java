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
import com.jd.journalq.service.BrokerMessageService;
import com.jd.journalq.util.NullUtil;
import com.jd.journalq.util.UrlEncoderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger= LoggerFactory.getLogger(BrokerMessageServiceImpl.class);
    private static final long TIMEOUT=10000;
    public static final ObjectMapper mapper = new ObjectMapper();
    @Resource(type = BrokerMonitorClusterQuery.class)
    private BrokerClusterQuery<Subscribe> brokerClusterQuery;
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
                            message = new SimplifiedBrokeMessage();
                            message.setQueryId(response.getKey());
                            message.setId(m.getPartition() + "-" + m.getMsgIndexNo());
                            message.setSendTime(m.getStartTime());
                            message.setStoreTime(m.getStoreTime());
                            message.setBusinessId(m.getBusinessId());
                            message.setBody(new String(m.getBody().getBytes(Charset.forName("utf-8"))));
                            message.setAttributes(m.getAttributes());
                            message.setFlag(false);
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
    public List<SimplifiedBrokeMessage> viewMessage(long topicId,String topic, String app, short partition, long index, int count) {
        return null;
    }



    @Override
    public SimplifiedBrokeMessage download(String ip, int port, String topic, String app, short partition, long index) {
        return null;
    }
}
