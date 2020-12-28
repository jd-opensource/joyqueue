/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.joyqueue.convert.CodeConverter;
import org.joyqueue.domain.Replica;
import org.joyqueue.manage.PartitionGroupMetric;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.Broker;
import org.joyqueue.model.domain.BrokerTopicMonitor;
import org.joyqueue.model.domain.BrokerTopicMonitorRecord;
import org.joyqueue.model.domain.Consumer;
import org.joyqueue.model.domain.Producer;
import org.joyqueue.model.domain.SubscribeType;
import org.joyqueue.model.query.QMonitor;
import org.joyqueue.monitor.BrokerMonitorInfo;
import org.joyqueue.monitor.BrokerStartupInfo;
import org.joyqueue.monitor.Client;
import org.joyqueue.monitor.ConnectionMonitorDetailInfo;
import org.joyqueue.monitor.ConsumerMonitorInfo;
import org.joyqueue.monitor.ProducerMonitorInfo;
import org.joyqueue.monitor.RestResponse;
import org.joyqueue.nsr.PartitionGroupServerService;
import org.joyqueue.other.HttpRestService;
import org.joyqueue.service.BrokerService;
import org.joyqueue.service.BrokerTopicMonitorService;
import org.joyqueue.service.ConsumerService;
import org.joyqueue.service.ProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @Autowired
    private PartitionGroupServerService partitionGroupServerService;


    @Override
    public PageResult<BrokerTopicMonitor> queryTopicsPartitionMointor(QPageQuery<QMonitor> qPageQuery) {

        PageResult<BrokerTopicMonitor> pageResult = new PageResult<>();
        try {
            Pagination pagination = qPageQuery.getPagination();
            QMonitor qMonitor = qPageQuery.getQuery();
            Broker broker = brokerService.findById(Integer.valueOf(String.valueOf(qMonitor.getBrokerId())));
            List<String> toplicList = queryTopicList(broker);
            pagination.setTotalRecord(toplicList.size());
            int toIndex = pagination.getStart() + pagination.getSize();
            if (toIndex > pagination.getTotalRecord()) {
                toIndex = pagination.getTotalRecord();
            }
            List<BrokerTopicMonitor> brokerTopicMonitorList = new ArrayList<>();
            for (String topic : toplicList.subList(pagination.getStart(), toIndex)) {
                BrokerTopicMonitor brokerTopicMonitor = new BrokerTopicMonitor();
                List<PartitionGroupMetric> partitionGroupMetricList = getPartitionGroup(topic, broker);
                brokerTopicMonitor.setTopic(topic);
                brokerTopicMonitor.setPartitionGroupMetricList(partitionGroupMetricList);
                brokerTopicMonitorList.add(brokerTopicMonitor);
            }
            pageResult.setPagination(pagination);
            pageResult.setResult(brokerTopicMonitorList);
        } catch (Exception e) {
            logger.error("queryTopicsPartitionMointor exception", e);
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
                List<Client> clients = connectionMonitorDetailInfo.getClients();
                pagination.setTotalRecord(clients.size());

                int toIndex = pagination.getStart() + pagination.getSize();
                if (toIndex > pagination.getTotalRecord()) {
                    toIndex = pagination.getTotalRecord();
                }
                pageResult.setPagination(pagination);
                pageResult.setResult(clients.subList(pagination.getStart(), toIndex));
            }
        } catch (Exception e) {
            logger.error("queryClientConnectionDetail exception", e);
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
    public PageResult<BrokerTopicMonitor> queryTopicsMointor(QPageQuery<QMonitor> qPageQuery) {
        try {
            Pagination pagination = qPageQuery.getPagination();
            QMonitor qMonitor = qPageQuery.getQuery();
            Broker broker = brokerService.findById(Integer.valueOf(String.valueOf(qMonitor.getBrokerId())));
            return getMonitorByBrokerPage(broker, qMonitor.getType(), pagination.getPage(), pagination.getSize());
        } catch (Exception e) {
            logger.error("queryTopicsMointor exception", e);
        }
        return new PageResult<>();
    }

    /**
     * 查询broker详情
     * @param brokerId
     * @return
     */
    @Override
    public BrokerMonitorInfo findBrokerMonitor(Long brokerId) {
        try {
            Broker broker = brokerService.findById(Integer.valueOf(String.valueOf(brokerId)));
            return queryBrokerMonitor(broker);
        } catch (Exception e) {
            logger.error("findBrokerMonitor exception", e);
        }
        return null;
    }

    /**
     * 查询启动信息
     * @param brokerId
     * @return
     */
    @Override
    public BrokerStartupInfo getStartupInfo(Long brokerId) throws Exception {
        Broker broker = brokerService.findById(Integer.valueOf(String.valueOf(brokerId)));
        return getStartInfo(broker);
    }

    @Override
    public List<String> queryTopicList(Long brokerId) throws Exception {
        Broker broker = brokerService.findById(brokerId.intValue());
        return queryTopicList(broker);
    }

    @Override
    public List<BrokerTopicMonitor> queryTopicsPartitionMonitors(Integer brokerId) {
        List<BrokerTopicMonitor> brokerTopicMonitorList = new ArrayList<>();
        try {
            Map<String, List<PartitionGroupMetric>> partitionGroupMetricMap = getPartitionGroupMetricMap(brokerId);
            for (Map.Entry<String, List<PartitionGroupMetric>> entry: partitionGroupMetricMap.entrySet()) {
                BrokerTopicMonitor brokerTopicMonitor = new BrokerTopicMonitor();
                brokerTopicMonitor.setTopic(entry.getKey());
                brokerTopicMonitor.setPartitionGroupMetricList(entry.getValue());
                brokerTopicMonitorList.add(brokerTopicMonitor);
            }
        } catch (Exception e) {
            logger.error("queryTopicsPartitionMointor exception", e);
        }
        return brokerTopicMonitorList;
    }

    private BrokerTopicMonitor getMonitorByAppAndTopic(String topic, List<String> appList, Broker broker, SubscribeType type) throws Exception {
        BrokerTopicMonitor brokerTopicMonitor = new BrokerTopicMonitor();
        List<BrokerTopicMonitorRecord> brokerMonitorRecordList = new ArrayList<>();
        for (String app : appList) {
            BrokerTopicMonitorRecord brokerTopicMonitorRecord = new BrokerTopicMonitorRecord();
            if (type == SubscribeType.CONSUMER) {
                ConsumerMonitorInfo consumerMonitorInfo = null;
                try {
                    consumerMonitorInfo = queryMonitorConsumer(topic, app, broker);
                }catch (Exception e){
                    logger.error("queryMonitorConsumer error",e);
                }
                if (consumerMonitorInfo != null) {
                    if (consumerMonitorInfo.getRetry() != null) {
                        brokerTopicMonitorRecord.setRetryCount(consumerMonitorInfo.getRetry().getCount());
                        brokerTopicMonitorRecord.setRetryTps(consumerMonitorInfo.getRetry().getCurrent());
                    }
                    if (consumerMonitorInfo.getPending() != null) {
                        brokerTopicMonitorRecord.setBacklog(consumerMonitorInfo.getPending().getCount());
                    }
                    brokerTopicMonitorRecord.setConnections(consumerMonitorInfo.getConnections());
                    brokerTopicMonitorRecord.setCount(consumerMonitorInfo.getDeQueue().getCount());
                    brokerTopicMonitorRecord.setTotalSize(consumerMonitorInfo.getDeQueue().getTotalSize());
                    brokerTopicMonitorRecord.setTraffic(consumerMonitorInfo.getDeQueue().getTraffic());
                }
            } else if (type == SubscribeType.PRODUCER) {
                ProducerMonitorInfo producerMonitorInfo = null;
                try {
                    producerMonitorInfo = queryMonitorProducer(topic, app, broker);
                }catch (Exception e){
                    logger.error("queryMonitorProducer error",e);
                }
                if (producerMonitorInfo != null) {
                    brokerTopicMonitorRecord.setConnections(producerMonitorInfo.getConnections());
                    brokerTopicMonitorRecord.setCount(producerMonitorInfo.getEnQueue().getCount());
                    brokerTopicMonitorRecord.setTotalSize(producerMonitorInfo.getEnQueue().getTotalSize());
                    brokerTopicMonitorRecord.setTraffic(producerMonitorInfo.getEnQueue().getTraffic());
                    brokerTopicMonitorRecord.setTps(producerMonitorInfo.getEnQueue().getTps());
                }
            }
            brokerTopicMonitorRecord.setApp(app);
            brokerMonitorRecordList.add(brokerTopicMonitorRecord);
        }
        brokerMonitorRecordList.sort(Comparator.comparingLong(BrokerTopicMonitorRecord::getBacklog));
        brokerTopicMonitor.setBrokerTopicMonitorRecordList(brokerMonitorRecordList);
        brokerTopicMonitor.setTopic(topic);
        return brokerTopicMonitor;
    }

    private PageResult<BrokerTopicMonitor> getMonitorByBrokerPage(Broker broker, SubscribeType type, int page, int pageSize) throws Exception {
        Pagination pagination = new Pagination();
        pagination.setPage(page);
        pagination.setSize(pageSize);
        PageResult<BrokerTopicMonitor> pageResult = new PageResult<>();
        List<BrokerTopicMonitor> brokerTopicMonitors = new ArrayList<>();
        if (type == SubscribeType.CONSUMER) {
            JSONObject map = queryMonitorConsumers(broker, page, pageSize);
            pagination.setTotalRecord(Integer.parseInt(map.getOrDefault("total", 0).toString()));
            List<ConsumerMonitorInfo> consumerMonitorInfos = map.getJSONArray("data").toJavaList(ConsumerMonitorInfo.class);
            for (ConsumerMonitorInfo consumerMonitorInfo: consumerMonitorInfos) {
                BrokerTopicMonitor brokerTopicMonitor = new BrokerTopicMonitor();
                BrokerTopicMonitorRecord brokerTopicMonitorRecord = new BrokerTopicMonitorRecord();
                if (consumerMonitorInfo.getRetry() != null) {
                    brokerTopicMonitorRecord.setRetryCount(consumerMonitorInfo.getRetry().getCount());
                    brokerTopicMonitorRecord.setRetryTps(consumerMonitorInfo.getRetry().getCurrent());
                }
                if (consumerMonitorInfo.getPending() != null) {
                    brokerTopicMonitorRecord.setBacklog(consumerMonitorInfo.getPending().getCount());
                }
                brokerTopicMonitorRecord.setConnections(consumerMonitorInfo.getConnections());
                brokerTopicMonitorRecord.setCount(consumerMonitorInfo.getDeQueue().getCount());
                brokerTopicMonitorRecord.setTotalSize(consumerMonitorInfo.getDeQueue().getTotalSize());
                brokerTopicMonitorRecord.setTraffic(consumerMonitorInfo.getDeQueue().getTraffic());
                brokerTopicMonitorRecord.setTps(consumerMonitorInfo.getDeQueue().getTps());
                brokerTopicMonitorRecord.setApp(consumerMonitorInfo.getApp());
                List<BrokerTopicMonitorRecord> brokerMonitorRecordList = new ArrayList<>();
                brokerMonitorRecordList.add(brokerTopicMonitorRecord);
                brokerTopicMonitor.setBrokerTopicMonitorRecordList(brokerMonitorRecordList);
                brokerTopicMonitor.setTopic(consumerMonitorInfo.getTopic());
                brokerTopicMonitors.add(brokerTopicMonitor);
            }
            brokerTopicMonitors.sort((o1, o2) -> {
                BrokerTopicMonitorRecord brokerTopicMonitorRecord1 = o1.getBrokerTopicMonitorRecordList().get(0);
                BrokerTopicMonitorRecord brokerTopicMonitorRecord2 = o2.getBrokerTopicMonitorRecordList().get(0);
                return Long.compare(brokerTopicMonitorRecord2.getBacklog(), brokerTopicMonitorRecord1.getBacklog());
            });
        } else if (type == SubscribeType.PRODUCER) {
            JSONObject map = queryMonitorProducers(broker, page, pageSize);
            pagination.setTotalRecord(Integer.parseInt(map.getOrDefault("total", 0).toString()));
            List<ProducerMonitorInfo> producerMonitorInfos = map.getJSONArray("data").toJavaList(ProducerMonitorInfo.class);
            for (ProducerMonitorInfo producerMonitorInfo: producerMonitorInfos) {
                BrokerTopicMonitor brokerTopicMonitor = new BrokerTopicMonitor();
                BrokerTopicMonitorRecord brokerTopicMonitorRecord = new BrokerTopicMonitorRecord();
                brokerTopicMonitorRecord.setConnections(producerMonitorInfo.getConnections());
                brokerTopicMonitorRecord.setCount(producerMonitorInfo.getEnQueue().getCount());
                brokerTopicMonitorRecord.setTotalSize(producerMonitorInfo.getEnQueue().getTotalSize());
                brokerTopicMonitorRecord.setTraffic(producerMonitorInfo.getEnQueue().getTraffic());
                brokerTopicMonitorRecord.setTps(producerMonitorInfo.getEnQueue().getTps());
                brokerTopicMonitorRecord.setApp(producerMonitorInfo.getApp());
                List<BrokerTopicMonitorRecord> brokerMonitorRecordList = new ArrayList<>();
                brokerMonitorRecordList.add(brokerTopicMonitorRecord);
                brokerTopicMonitor.setBrokerTopicMonitorRecordList(brokerMonitorRecordList);
                brokerTopicMonitor.setTopic(producerMonitorInfo.getTopic());
                brokerTopicMonitors.add(brokerTopicMonitor);
            }
            brokerTopicMonitors.sort((o1, o2) -> {
                BrokerTopicMonitorRecord brokerTopicMonitorRecord1 = o1.getBrokerTopicMonitorRecordList().get(0);
                BrokerTopicMonitorRecord brokerTopicMonitorRecord2 = o2.getBrokerTopicMonitorRecordList().get(0);
                return Long.compare(brokerTopicMonitorRecord2.getCount(), brokerTopicMonitorRecord1.getCount());
            });
        }
        pageResult.setResult(brokerTopicMonitors);
        pageResult.setPagination(pagination);
        return pageResult;
    }


    private List<String> getAppByTopic(SubscribeType subscribeType, String topic) throws Exception {
        if (subscribeType == SubscribeType.CONSUMER) {
            List<Consumer> consumerList;
            try {
                consumerList = consumerService.findByTopic(topic, null);
            }catch (Exception e) {
                logger.error("Cause error",e);
                consumerList = Collections.emptyList();
            }
            return consumerList.stream().map(consumer -> CodeConverter.convertApp(consumer.getApp(), consumer.getSubscribeGroup())).collect(Collectors.toList());

        } else if (subscribeType == SubscribeType.PRODUCER) {
            List<Producer> producerList;
            try {
                producerList = producerService.findByTopic(null, topic);
            }catch (Exception e){
                logger.error("Cause error",e);
                producerList = Collections.emptyList();
            }
            return producerList.stream().map(producer -> producer.getApp().getCode()).collect(Collectors.toList());
        }
        return new ArrayList<>();

    }

    private List<PartitionGroupMetric> getPartitionGroup(String topic, Broker broker) {
        String path = "partitionGroupDetail";
        String[] args = new String[3];
        args[0] = broker.getIp();
        args[1] = String.valueOf(broker.getMonitorPort());
        args[2] = topic;
        RestResponse<List<PartitionGroupMetric>> restResponse = httpRestService.get(path, PartitionGroupMetric.class, true, args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return null;
    }

    private Map<String, List<PartitionGroupMetric>> getPartitionGroupMetricMap(Integer brokerId) {
        Map<String, List<Replica>> replicaMap = partitionGroupServerService.getByBrokerId(brokerId)
                .stream().collect(Collectors.groupingBy(replica -> replica.getTopic().getCode()));
        Map<String, List<PartitionGroupMetric>> map = new HashMap<>();
        for (Map.Entry<String, List<Replica>> entry: replicaMap.entrySet()) {
            List<PartitionGroupMetric> metrics = new ArrayList<>(entry.getValue().size());
            for (Replica replica: entry.getValue()) {
                PartitionGroupMetric metric = new PartitionGroupMetric();
                metric.setPartitionGroup(replica.getGroup());
                metrics.add(metric);
            }
            map.put(entry.getKey(), metrics);
        }
        return map;
    }

    private JSONObject queryMonitorConsumers(Broker broker, int page, int pageSize) {
        String path = "consumerInfos";
        String[] args = new String[4];
        args[0] = broker.getIp();
        args[1] = String.valueOf(broker.getMonitorPort());
        args[2] = String.valueOf(page);
        args[3] = String.valueOf(pageSize);
        RestResponse<JSONObject> restResponse = httpRestService.get(path, JSONObject.class, false, args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return new JSONObject();
    }

    private JSONObject queryMonitorProducers(Broker broker, int page, int pageSize) {
        String path = "producerInfos";
        String[] args = new String[4];
        args[0] = broker.getIp();
        args[1] = String.valueOf(broker.getMonitorPort());
        args[2] = String.valueOf(page);
        args[3] = String.valueOf(pageSize);
        RestResponse<JSONObject> restResponse = httpRestService.get(path, JSONObject.class, false, args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return new JSONObject();
    }

    /**
     * 查询消费者详情
     * @return
     */
    private ConsumerMonitorInfo queryMonitorConsumer(String topic, String app, Broker broker) throws Exception {
        String path = "appTopicMonitorConsumer";
        String[] args = new String[4];
        args[0] = broker.getIp();
        args[1] = String.valueOf(broker.getMonitorPort());
        args[2] = topic;
        args[3] = app;
        RestResponse<ConsumerMonitorInfo> restResponse = httpRestService.get(path, ConsumerMonitorInfo.class, false, args);
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
        String path = "appConnectionDetail";
        String[] args = new String[4];
        args[0] = broker.getIp();
        args[1] = String.valueOf(broker.getMonitorPort());
        RestResponse<ConnectionMonitorDetailInfo> restResponse = httpRestService.get(path, ConnectionMonitorDetailInfo.class, false, args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return null;
    }

    /**
     * 查询生产者详情
     * @return
     */
    private ProducerMonitorInfo queryMonitorProducer(String topic, String app, Broker broker) throws Exception {
        String path = "appTopicMonitorProducer";
        String[] args = new String[4];
        args[0] = broker.getIp();
        args[1] = String.valueOf(broker.getMonitorPort());
        args[2] = topic;
        args[3] = app;
        RestResponse<ProducerMonitorInfo> restResponse = httpRestService.get(path, ProducerMonitorInfo.class, false, args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return null;
    }

    /**
     * 查询topicList
     * @return
     */
    public List<String> queryTopicList(Broker broker) throws Exception {
        String path = "topicList";
        String[] args = new String[2];
        args[0] = broker.getIp();
        args[1] = String.valueOf(broker.getMonitorPort());
        RestResponse<List<String>> restResponse = httpRestService.get(path, String.class, true, args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return null;
    }

    /**
     * 查询broker监控
     * @return
     */
    private BrokerMonitorInfo queryBrokerMonitor(Broker broker) throws Exception {
        String path = "brokerMonitor";
        String[] args = new String[2];
        args[0] = broker.getIp();
        args[1] = String.valueOf(broker.getMonitorPort());
        RestResponse<BrokerMonitorInfo> restResponse = httpRestService.get(path, BrokerMonitorInfo.class, false, args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return null;
    }

    /**
     * 查询broker监控
     * @return
     */
    private BrokerStartupInfo getStartInfo(Broker broker) throws Exception {
        String path = "startupInfo";
        String[] args = new String[2];
        args[0] = broker.getIp();
        args[1] = String.valueOf(broker.getMonitorPort());
        RestResponse<BrokerStartupInfo> restResponse = httpRestService.get(path, BrokerStartupInfo.class, false, args);
        if (restResponse != null && restResponse.getData() != null) {
            return restResponse.getData();
        }
        return null;
    }

}
