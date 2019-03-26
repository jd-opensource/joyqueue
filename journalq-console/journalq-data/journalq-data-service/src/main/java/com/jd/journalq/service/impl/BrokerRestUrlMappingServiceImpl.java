package com.jd.journalq.service.impl;

import com.jd.journalq.model.domain.Broker;
import com.jd.journalq.service.BrokerRestUrlMappingService;
import com.jd.journalq.util.NullUtil;
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

    /** message preview path  */
    private static final String pendingByteMessagePath="/manage/topic/%s/app/%s/message/pending?count=%s";
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

    /** offset management*/
    private String appConsumeOffsetMonitorPath="/manage/topic/%s/app/%s/acks"; // topic|app
    private String appPartitionOffsetMonitorPath="/manage/topic/%s/app/%s/partition/%s/ack";// topic|app|partition,method=get|put
    private String resetAppPartitionOffsetPath="/manage/topic/%s/app/%s/partition/%s/ack?index=%s";// topic|app|partition,method=get|put
    private String resetAppPartitionOffsetByTimePath="/manage/topic/%s/app/%s/partition/%s/ackByTime?timestamp=%s";// topic|app|partition,method=put
    private String resetAppTopicOffsetByTimePath="/manage/topic/%s/app/%s/ackByTime?timestamp=%s";// topic|app|partition,method=put
    private String getTopicAppOffsetPath = "/manage/topic/%s/app/%s/timestamp/%s/ackByTime"; ///manage/topic/:topic/app/:app/timestamp/%s/ackByTime,method=get
    /** topic */
    private String topicPartitionGroupsMonitorPath ="/monitor/topic/%s/app/%s/partitionGroups";

    /** partition group*/
    private String partitionGroupCoordinatorInfoMonitorPath="/monitor/coordinator/group/%s/detail"; // groupId
    private String partitionGroupCoordinatorDetailMonitorPath="/monitor/coordinator/namespace/%s/group/%s?topic=%s&isFormat=true";    // namespace|groupId
//    /manage/topic/:topic/partitionGroup/detail
    private String partitionGroupDetailPath = "/manage/topic/%s/partitionGroup/detail";


    /** archive monitor*/
    private String archiveMonitorPath="/monitor/archive/";

    /** store **/
    private String topicListPath = "/manage/topic/list";


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
            throw new IllegalStateException("parse broker routing config file failure!");
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
