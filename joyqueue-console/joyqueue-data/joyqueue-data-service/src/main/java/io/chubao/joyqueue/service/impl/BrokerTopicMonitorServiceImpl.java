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
package io.chubao.joyqueue.service.impl;

import io.chubao.joyqueue.convert.CodeConverter;
import io.chubao.joyqueue.manage.PartitionGroupMetric;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.Pagination;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.Broker;
import io.chubao.joyqueue.model.domain.BrokerTopicMonitor;
import io.chubao.joyqueue.model.domain.BrokerTopicMonitorRecord;
import io.chubao.joyqueue.model.domain.Consumer;
import io.chubao.joyqueue.model.domain.Producer;
import io.chubao.joyqueue.model.domain.SubscribeType;
import io.chubao.joyqueue.model.query.QMonitor;
import io.chubao.joyqueue.monitor.BrokerMonitorInfo;
import io.chubao.joyqueue.monitor.BrokerStartupInfo;
import io.chubao.joyqueue.monitor.Client;
import io.chubao.joyqueue.monitor.ConnectionMonitorDetailInfo;
import io.chubao.joyqueue.monitor.ConsumerMonitorInfo;
import io.chubao.joyqueue.monitor.ProducerMonitorInfo;
import io.chubao.joyqueue.monitor.RestResponse;
import io.chubao.joyqueue.other.HttpRestService;
import io.chubao.joyqueue.service.BrokerService;
import io.chubao.joyqueue.service.BrokerTopicMonitorService;
import io.chubao.joyqueue.service.ConsumerService;
import io.chubao.joyqueue.service.ProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wangxiaofei1 on 2019/3/13.
 */
@Service("brokerTopicMonitorService")
public class BrokerTopicMonitorServiceImpl implements BrokerTopicMonitorService {
    public static final Logger logger = LoggerFactory.getLogger(BrokerTopicMonitorServiceImpl.class);
    @Autowired
    private HttpRestService httpRestService;
    @Autowired
    protected BrokerService brokerService;
    @Autowired
    private ConsumerService consumerService;
    @Autowired
    private ProducerService producerService;


    @Override
    public PageResult<BrokerTopicMonitor> queryTopicsPartitionMointor(QPageQuery<QMonitor> qPageQuery)  {

        PageResult<BrokerTopicMonitor> pageResult = new PageResult<>();
        try {
            Pagination pagination = qPageQuery.getPagination();
            QMonitor qMonitor = qPageQuery.getQuery();
            Broker broker = brokerService.findById(Integer.valueOf(String.valueOf(qMonitor.getBrokerId())));
            List<String> toplicList = queryTopicList(broker);
            pagination.setTotalRecord(toplicList.size());
            int toIndex= pagination.getStart()+pagination.getSize();
            if (toIndex >pagination.getTotalRecord()) {
                toIndex = pagination.getTotalRecord();
            }
            List<BrokerTopicMonitor> brokerTopicMonitorList = new ArrayList<>();
            for (String topic: toplicList.subList(pagination.getStart(),toIndex)) {
                BrokerTopicMonitor brokerTopicMonitor = new BrokerTopicMonitor();
                List<PartitionGroupMetric> partitionGroupMetricList = getPartitionGroup(topic,broker);
                brokerTopicMonitor.setTopic(topic);
                brokerTopicMonitor.setPartitionGroupMetricList(partitionGroupMetricList);
                brokerTopicMonitorList.add(brokerTopicMonitor);
            }
            pageResult.setPagination(pagination);
            pageResult.setResult(brokerTopicMonitorList);
        } catch (Exception e) {
            logger.error("queryTopicsPartitionMointor exception",e);
        }
        return pageResult;
    }

    /**
     *
     * 查询连接详情
     * @param qPageQuery
     * @return
     */
    @Override
    public PageResult<Client> queryClientConnectionDetail(QPageQuery<QMonitor> qPageQuery) {
        PageResult<Client> pageResult = new PageResult<>();
        try {
            Pagination pagination = qPageQuery.getPagination();
            QMonitor qMonitor = qPageQuery.getQuery();
            Broker broker = brokerService.findById(Integer.valueOf(String.valueOf(qMonitor.getBrokerId())));
            ConnectionMonitorDetailInfo connectionMonitorDetailInfo = getConnectMonitorDetail(broker);
            if (connectionMonitorDetailInfo != null) {
                List<Client> clients=connectionMonitorDetailInfo.getClients();
                pagination.setTotalRecord(clients.size());

                int toIndex= pagination.getStart()+pagination.getSize();
                if (toIndex >pagination.getTotalRecord()) {
                    toIndex = pagination.getTotalRecord();
                }
                pageResult.setPagination(pagination);
                pageResult.setResult(clients.subList(pagination.getStart(),toIndex));
            }
        } catch (Exception e) {
            logger.error("queryClientConnectionDetail exception",e);
        }
        return pageResult;

    }

    /**
     * 查询topic,app 详情
     * @param qPageQuery
     * @return
     * @throws Exception
     */
    @Override
    public PageResult<BrokerTopicMonitor> queryTopicsMointor(QPageQuery<QMonitor> qPageQuery)  {

        PageResult<BrokerTopicMonitor> pageResult = new PageResult<>();
        try {
            Pagination pagination = qPageQuery.getPagination();
            QMonitor qMonitor = qPageQuery.getQuery();
            Broker broker = brokerService.findById(Integer.valueOf(String.valueOf(qMonitor.getBrokerId())));
            List<String> toplicList = queryTopicList(broker);
            pagination.setTotalRecord(toplicList.size());
            int fromIndx= pagination.getStart()+pagination.getSize();
            if (fromIndx >pagination.getTotalRecord()) {
                fromIndx = pagination.getTotalRecord();
            }

            List<BrokerTopicMonitor> brokerTopicMonitors = new ArrayList<>(pagination.getSize());

            for (String topic: toplicList.subList(pagination.getStart(),fromIndx)) {
                List<String> appList = getAppByTopic(qMonitor.getType(),topic);

                BrokerTopicMonitor brokerTopicMonitor = getMonitorByAppAndTopic(topic,appList,broker,qMonitor.getType());

                brokerTopicMonitors.add(brokerTopicMonitor);
            }
            pageResult.setPagination(pagination);
            pageResult.setResult(brokerTopicMonitors);
        } catch (Exception e) {
            logger.error("queryTopicsMointor exception",e);
        }
        return pageResult;
    }

    /**
     * 查询broker详情
     * @param brokerId
     * @return
     */
    public BrokerMonitorInfo findBrokerMonitor(Long brokerId){
        try {
            Broker broker = brokerService.findById(Integer.valueOf(String.valueOf(brokerId)));
            return queryBrokerMonitor(broker);
        } catch (Exception e) {
            logger.error("findBrokerMonitor exception",e);
        }
        return null;
    }
    /**
     * 查询启动信息
     * @param brokerId
     * @return
     */
    public BrokerStartupInfo getStartupInfo(Long brokerId) throws Exception {
        Broker broker = brokerService.findById(Integer.valueOf(String.valueOf(brokerId)));
        return getStartInfo(broker);
    }
    private BrokerTopicMonitor getMonitorByAppAndTopic(String topic,List<String> appList,Broker broker,SubscribeType type) throws Exception {
        BrokerTopicMonitor brokerTopicMonitor = new BrokerTopicMonitor();
        List<BrokerTopicMonitorRecord> brokerMonitorRecordList = new ArrayList<>();
        for (String app:appList) {
            BrokerTopicMonitorRecord brokerTopicMonitorRecord = new BrokerTopicMonitorRecord();
            if (type == SubscribeType.CONSUMER) {
                ConsumerMonitorInfo consumerMonitorInfo = queryMonitorConsumer(topic,app,broker);
                brokerTopicMonitorRecord.setConnections(consumerMonitorInfo.getConnections());
                brokerTopicMonitorRecord.setCount(consumerMonitorInfo.getDeQueue().getCount());
                brokerTopicMonitorRecord.setTotalSize(consumerMonitorInfo.getDeQueue().getTotalSize());
            } else if (type == SubscribeType.PRODUCER) {
                ProducerMonitorInfo producerMonitorInfo = queryMonitorProducer(topic,app,broker);
                brokerTopicMonitorRecord.setConnections(producerMonitorInfo.getConnections());
                brokerTopicMonitorRecord.setCount(producerMonitorInfo.getEnQueue().getCount());
                brokerTopicMonitorRecord.setTotalSize(producerMonitorInfo.getEnQueue().getTotalSize());
            }
            brokerTopicMonitorRecord.setApp(app);
            brokerMonitorRecordList.add(brokerTopicMonitorRecord);
        }
        brokerTopicMonitor.setBrokerTopicMonitorRecordList(brokerMonitorRecordList);
        brokerTopicMonitor.setTopic(topic);
        return brokerTopicMonitor;
    }
    private List<String> getAppByTopic(SubscribeType subscribeType,String topic) throws Exception {
        if (subscribeType == SubscribeType.CONSUMER) {
            List<Consumer> consumerList =  consumerService.findByTopic(topic, null);
            return consumerList.stream().map(consumer -> CodeConverter.convertApp(consumer.getApp(),consumer.getSubscribeGroup())).collect(Collectors.toList());

        } else if (subscribeType == SubscribeType.PRODUCER) {
            List<Producer> producerList =  producerService.findByTopic(null, topic);
            return producerList.stream().map(producer -> producer.getApp().getCode()).collect(Collectors.toList());
        }
        return new ArrayList<>();

    }
    private List<PartitionGroupMetric> getPartitionGroup(String topic, Broker broker){
        String path="partitionGroupDetail";
        String[] args=new String[3];
        args[0]=broker.getIp();
        args[1]=String.valueOf(broker.getMonitorPort());
        args[2]=topic;
        RestResponse<List<PartitionGroupMetric>> restResponse = httpRestService.get(path,PartitionGroupMetric.class,true,args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return null;
    }

    /**
     * 查询消费者详情
     * @return
     */
    private ConsumerMonitorInfo queryMonitorConsumer(String topic, String app, Broker broker) throws Exception {
        String path="appTopicMonitorConsumer";
        String[] args=new String[4];
        args[0]=broker.getIp();
        args[1]=String.valueOf(broker.getMonitorPort());
        args[2]=topic;
        args[3]=app;
        RestResponse<ConsumerMonitorInfo> restResponse = httpRestService.get(path,ConsumerMonitorInfo.class,false,args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return null;
    }

    /**
     * 查询连接详情
     * @return
     */
    private ConnectionMonitorDetailInfo getConnectMonitorDetail(Broker broker) throws Exception {
        String path="appConnectionDetail";
        String[] args=new String[4];
        args[0]=broker.getIp();
        args[1]=String.valueOf(broker.getMonitorPort());
        RestResponse<ConnectionMonitorDetailInfo> restResponse = httpRestService.get(path,ConnectionMonitorDetailInfo.class,false,args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return null;
    }

    /**
     * 查询生产者详情
     * @return
     */
    private ProducerMonitorInfo queryMonitorProducer(String topic,String app,Broker broker) throws Exception {
        String path="appTopicMonitorProducer";
        String[] args=new String[4];
        args[0]=broker.getIp();
        args[1]=String.valueOf(broker.getMonitorPort());
        args[2]=topic;
        args[3]=app;
        RestResponse<ProducerMonitorInfo> restResponse = httpRestService.get(path,ProducerMonitorInfo.class,false,args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return null;
    }

    /**
     * 查询topicList
     * @return
     */
    private List<String> queryTopicList(Broker  broker) throws Exception {
        String path="topicList";
        String[] args=new String[2];
        args[0]=broker.getIp();
        args[1]=String.valueOf(broker.getMonitorPort());
        RestResponse<List<String>> restResponse = httpRestService.get(path,String.class,true,args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return null;
    }

    /**
     * 查询broker监控
     * @return
     */
    private BrokerMonitorInfo queryBrokerMonitor(Broker  broker) throws Exception {
        String path="brokerMonitor";
        String[] args=new String[2];
        args[0]=broker.getIp();
        args[1]=String.valueOf(broker.getMonitorPort());
        RestResponse<BrokerMonitorInfo> restResponse = httpRestService.get(path,BrokerMonitorInfo.class,false,args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return null;
    }

    /**
     * 查询broker监控
     * @return
     */
    private BrokerStartupInfo getStartInfo(Broker  broker) throws Exception {
        String path="startupInfo";
        String[] args=new String[2];
        args[0]=broker.getIp();
        args[1]=String.valueOf(broker.getMonitorPort());
        RestResponse<BrokerStartupInfo> restResponse = httpRestService.get(path,BrokerStartupInfo.class,false,args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return null;
    }

}
