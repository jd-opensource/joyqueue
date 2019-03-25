package com.jd.journalq.service.impl;

import com.jd.journalq.common.manage.PartitionGroupMetric;
import com.jd.journalq.common.model.PageResult;
import com.jd.journalq.common.model.Pagination;
import com.jd.journalq.common.model.QPageQuery;
import com.jd.journalq.common.monitor.*;
import com.jd.journalq.model.domain.*;
import com.jd.journalq.model.query.QConsumer;
import com.jd.journalq.model.query.QMonitor;
import com.jd.journalq.model.query.QProducer;
import com.jd.journalq.other.HttpRestService;
import com.jd.journalq.service.BrokerService;
import com.jd.journalq.service.BrokerTopicMonitorService;
import com.jd.journalq.service.ConsumerService;
import com.jd.journalq.service.ProducerService;
import com.jd.journalq.common.monitor.Client;
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
            Broker broker = brokerService.findById(qMonitor.getBrokerId());
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
            e.printStackTrace();
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
            Broker broker = brokerService.findById(qMonitor.getBrokerId());
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
            e.printStackTrace();
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
            Broker broker = brokerService.findById(qMonitor.getBrokerId());
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
            e.printStackTrace();
        }
        return pageResult;
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
            QConsumer qConsumer = new QConsumer();
            qConsumer.setTopic(new Topic(topic));
            List<Consumer> consumerList =  consumerService.findByQuery(qConsumer);
            return consumerList.stream().map(consumer -> consumer.getApp().getCode()).collect(Collectors.toList());

        } else if (subscribeType == SubscribeType.PRODUCER) {
            QProducer qProducer = new QProducer();
            qProducer.setTopic(new Topic(topic));
            List<Producer> producerList =  producerService.findByQuery(qProducer);
            return producerList.stream().map(consumer -> consumer.getApp().getCode()).collect(Collectors.toList());
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
    public List<String> queryTopicList(Broker  broker) throws Exception {
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

}
