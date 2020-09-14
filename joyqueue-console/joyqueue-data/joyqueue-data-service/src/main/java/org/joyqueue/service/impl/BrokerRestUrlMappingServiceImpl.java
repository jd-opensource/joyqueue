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

import org.joyqueue.model.domain.Broker;
import org.joyqueue.service.BrokerRestUrlMappingService;
import org.joyqueue.util.NullUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 托管url mapping ,要求field name以Path结尾，
 * 参数用String.format占位符
 *
 **/
@Service("brokerRestUrlMappingService")
public class BrokerRestUrlMappingServiceImpl implements BrokerRestUrlMappingService {
    private Logger logger= LoggerFactory.getLogger(BrokerRestUrlMappingServiceImpl.class);

    /** message preview pending path  */
    private static final String pendingByteMessagePath="/manage/topic/%s/app/%s/message/pending?count=%s";
    /** message preview path  */
    private static final String previewMessagePath="/manage/topic/%s/app/%s/message/view?count=%s";

    private String lastestMessagePath="/manage/topic/%s/app/%s/message/last";
    private String partitoinMessagePath="/manage/topic/%s/app/%s/partition/%d/message";

    /** monitor path */
    private String appMonitorPath="/monitor/topic/%s/app/%s/%s"; // topic|app|type{producer/consumer}
    private String appPartitionMonitorPath="/monitor/topic/%s/app/%s/%s/partitions"; // topic|app|type{producer/consumer}
    private String appClientMonitorPath="/monitor/topic/%s/app/%s/%s/connections/detail";

    private String appConnectionPath="/monitor/topic/%s/app/%s/connections";   //monitor/topic/:topic/app/:app/connections
    private String consumerPartitionGroupsMonitorPath="/monitor/topic/%s/app/%s/consumer/partitionGroup/%s";///monitor/topic/:topic/app/:app/consumer/partitionGroup/:partitionGroupId
    private String partitionGroupsMonitorPath="/monitor/topic/%s/app/%s/partitionGroup/%s";  //monitor/topic/:topic/app/:app/partitionGroup/:partitionGroupId
    private String appPartitionGroupsMonitorPath="/monitor/topic/%s/app/%s/%s/partitionGroup/%s";///monitor/topic/:topic/app/:app/{consumer|producer}/partitionGroup/:partitionGroupId
//    /monitor/topic/:topic/app/:app/consumer
    private String appTopicMonitorConsumerPath ="/monitor/topic/%s/app/%s/consumer";
//    /monitor/topic/:topic/app/:app/producer
    private String appTopicMonitorProducerPath ="/monitor/topic/%s/app/%s/producer";
//    /monitor/connections/detail
    private String appConnectionDetailPath = "/monitor/connections/detail";
    //    /manage/topic/:topic/partitionGroup/:partitionGroup/store/metric
    private String partitiongroupIndexPath="/manage/topic/%s/partitionGroup/%s/store/metric";
    private String consumerInfosPath = "/monitor/consumers?page=%s&pageSize=%s";
    private String producerInfosPath = "/monitor/producers?page=%s&pageSize=%s";

    /** offset management*/
    private String removeProducersConnectionsPath= "/manage/topic/%s/app/%s/producers";
    private String removeConsumersConnectionsPath= "/manage/topic/%s/app/%s/consumers";
    private String appConsumeOffsetMonitorPath="/manage/topic/%s/app/%s/acks"; // topic|app
    private String appPartitionOffsetMonitorPath="/manage/topic/%s/app/%s/partition/%s/ack";// topic|app|partition,method=get|put
    private String resetAppPartitionOffsetPath="/manage/topic/%s/app/%s/partition/%s/ack?index=%s";// topic|app|partition,method=get|put
    private String resetAppPartitionOffsetByTimePath="/manage/topic/%s/app/%s/partition/%s/ackByTime?timestamp=%s";// topic|app|partition,method=put
    private String resetAppTopicOffsetByTimePath="/manage/topic/%s/app/%s/ackByTime?timestamp=%s";// topic|app|partition,method=put
    private String getTopicAppOffsetPath = "/manage/topic/%s/app/%s/timestamp/%s/ackByTime"; ///manage/topic/:topic/app/:app/timestamp/%s/ackByTime,method=get
//    /manage/topic/:topic/app/:app/partition/:partition/message ?count=&index=
    private String getPartitionMessageByIndexPath="/manage/topic/%s/app/%s/partition/%s/message?index=%s&count=%s";

//    /manage/topic/:topic/app/:app/partition/:partition/ackByTime
    private String getTopicAppPartitionIndexByTimePath="/manage/topic/%s/app/%s/partition/%s/ackByTime?timestamp=%s";
    /** topic */
    private String topicPartitionGroupsMonitorPath ="/monitor/topic/%s/app/%s/partitionGroups";
    private String topicPartitionGroupMetadataPath="/monitor/topic/%s/metadata?isCluster=true";

    /** partition group*/
    private String partitionGroupCoordinatorInfoMonitorPath="/monitor/coordinator/group/%s/detail"; // groupId
    private String partitionGroupCoordinatorDetailMonitorPath="/monitor/coordinator/namespace/%s/group/%s?topic=%s&isFormat=true";    // namespace|groupId
//    /manage/topic/:topic/partitionGroup/detail
    private String partitionGroupDetailPath = "/manage/topic/%s/partitionGroup/detail";


    /** archive monitor*/
    private String archiveMonitorPath="/monitor/archive/info";

    /** store **/
    private String topicListPath = "/manage/topic/list";
    private String storeTreeViewPath = "/manage/store/tree/view/%s"; // recursive
    private String deleteGarbageFilePath = "/manage/store/delete/garbage/file/%s/%s"; // filename|retain

    /** broker **/
    private String brokerMonitorPath = "/monitor/broker";
    private String startupInfoPath = "/startInfo";


    /** proxy monitor */
    private String mqttProxySummaryMonitorPath="/monitor/mqtt/proxy/summary";
    private String mqttProxyConnectionsMonitorPath="/monitor/mqtt/proxy/connections?page=%s&pageSize=%s";
    private String mqttProxyConnectionMonitorPath="/monitor/mqtt/proxy/connections/%s"; //params:clientId
    private String mqttProxySessionsMonitorPath="/monitor/mqtt/proxy/sessions?page=%s&pageSize=%s";
    private String mqttProxySessionMonitorPath="/monitor/mqtt/proxy/sessions/%s";        //params:clientId
    private String mqttProxyThreadsMonitorPath="/monitor/mqtt/proxy/%s/threads/summary";     // consume|delivery
    private String mqttProxyThreadClientsMonitorPath="/monitor/mqtt/proxy/%s/clients/detail/thread/%s?page=%s&pageSize=%s";//consume|delivery/:threadId
    private String mqttProxyApplicationPublishMonitorPath="/monitor/mqtt/proxy/publish/total/app/%s"; //application
    private String mqttProxyApplicationTopicPublishMonitorPath="/monitor/mqtt/proxy/publish/total/app/%s/topic/%s"; //:application/:topic
    private String mqttProxyApplicationClientPublishMonitorPath="/monitor/mqtt/proxy/publish/total/client/%s"; //clientID
    private String mqttProxyConnectionDisconnectManagementPath="/manage/mqtt/proxy/connection/disconnect/%s";  //clientID

    private String mqttProxyClientDebugStatusManagementPath="/manage/mqtt/proxy/set/consume/client/debug/status/%s?status=%s";  //clientID|put
    private String mqttProxyThreadDebugStatusManagementPath="/manage/mqtt/proxy/set/consume/thread/debug/status/%s?status=%s";  //threadId|put


    private String taskExecutorStateMonitorPath="/task/executor/state/monitor";
    /**
     * host:port template
     **/
    private String brokerRestUrl="http://%s:%s";



    private Map<String/*field name with out path suffix*/,String/*field value*/> urlMapping=new HashMap<>(64);
    public BrokerRestUrlMappingServiceImpl(){
        try {
            loadUrlMapping();
        }catch (IllegalAccessException e){
            logger.error("", e);
            throw new IllegalStateException("parse broker routing config file failure!", e);
        }
    }

    private void loadUrlMapping() throws IllegalAccessException {
         Field[] fields= this.getClass().getDeclaredFields();
         String   key;
         for(Field f:fields){
             if(f.getName().endsWith("Path")){
                 key=f.getName().substring(0,f.getName().length()-4);
                 f.setAccessible(true);
                 urlMapping.put(key,(String)f.get(this));
                 logger.info(String.format("register broker rest monitorUrl,key:%s,value:%s",key,(String)f.get(this)));
             }
         }
    }

    @Override
    public String pathTemplate(String key) {
        String subPath=urlMapping.get(key);
        if(NullUtil.isEmpty(subPath)){
            logger.info("not found monitorUrl template "+key);
            return null;
        }
        return subPath;
    }

    @Override
    public String urlTemplate(String key) {
        return brokerRestUrl+pathTemplate(key);
    }

    @Override
    public String monitorUrl(Broker broker) {
        return String.format(brokerRestUrl,broker.getIp(),broker.getMonitorPort());
    }

    @Override
    public String url(String ip, int port) {
        return String.format(brokerRestUrl,ip,port);
    }
}
