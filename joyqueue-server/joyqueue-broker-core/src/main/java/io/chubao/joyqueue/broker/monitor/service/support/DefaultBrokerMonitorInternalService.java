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
package io.chubao.joyqueue.broker.monitor.service.support;

import com.sun.management.GcInfo;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.consumer.Consume;
import io.chubao.joyqueue.broker.election.ElectionService;
import io.chubao.joyqueue.broker.monitor.converter.BrokerMonitorConverter;
import io.chubao.joyqueue.broker.monitor.exception.MonitorException;
import io.chubao.joyqueue.broker.monitor.service.BrokerMonitorInternalService;
import io.chubao.joyqueue.broker.monitor.stat.*;
import io.chubao.joyqueue.monitor.*;
import io.chubao.joyqueue.network.session.Consumer;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.store.StoreManagementService;
import io.chubao.joyqueue.store.StoreService;
import io.chubao.joyqueue.toolkit.format.Format;
import io.chubao.joyqueue.toolkit.lang.Online;
import io.chubao.joyqueue.toolkit.vm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Map;

/**
 * BrokerMonitorInternalService
 *
 * author: gaohaoxiang
 * date: 2018/10/15
 */
public class DefaultBrokerMonitorInternalService implements BrokerMonitorInternalService {
    private static final Logger logger= LoggerFactory.getLogger(DefaultBrokerMonitorInternalService.class);

    private BrokerStat brokerStat;
    private Consume consume;
    private StoreManagementService storeManagementService;
    private NameService nameService;
    private StoreService storeService;
    private ElectionService electionService;
    private ClusterManager clusterManager;
    private BrokerStartupInfo brokerStartupInfo;
    private JVMMonitorService jvmMonitorService;
    private DefaultGCNotificationParser gcNotificationParser;

    public DefaultBrokerMonitorInternalService(BrokerStat brokerStat, Consume consume,
                                               StoreManagementService storeManagementService,
                                               NameService nameService, StoreService storeService,
                                               ElectionService electionManager, ClusterManager clusterManager, BrokerStartupInfo brokerStartupInfo) {
        this.brokerStat = brokerStat;
        this.consume = consume;
        this.storeManagementService=storeManagementService;
        this.nameService = nameService;
        this.storeService = storeService;
        this.electionService = electionManager;
        this.clusterManager = clusterManager;
        this.brokerStartupInfo = brokerStartupInfo;
        this.jvmMonitorService=new GarbageCollectorMonitor();
        this.gcNotificationParser=new DefaultGCNotificationParser();
        this.gcNotificationParser.addListener(new DefaultGCEventListener(brokerStat.getJvmStat()));
        this.jvmMonitorService.addGCEventListener(gcNotificationParser);
    }

    @Override
    public BrokerMonitorInfo getBrokerInfo() {
        BrokerMonitorInfo brokerMonitorInfo = new BrokerMonitorInfo();
        brokerMonitorInfo.setConnection(BrokerMonitorConverter.convertConnectionMonitorInfo(brokerStat.getConnectionStat()));
        brokerMonitorInfo.setEnQueue(BrokerMonitorConverter.convertEnQueueMonitorInfo(brokerStat.getEnQueueStat()));
        brokerMonitorInfo.setDeQueue(BrokerMonitorConverter.convertDeQueueMonitorInfo(brokerStat.getDeQueueStat()));
        brokerMonitorInfo.setReplication(BrokerMonitorConverter.convertReplicationMonitorInfo(brokerStat.getReplicationStat()));

        StoreMonitorInfo storeMonitorInfo = new StoreMonitorInfo();
        storeMonitorInfo.setStarted(storeService instanceof Online?((Online) storeService).isStarted():true);
        storeMonitorInfo.setFreeSpace(Format.formatSize(storeManagementService.freeSpace()));
        storeMonitorInfo.setTotalSpace(Format.formatSize(storeManagementService.totalSpace()));

        NameServerMonitorInfo nameServerMonitorInfo = new NameServerMonitorInfo();
        nameServerMonitorInfo.setStarted(nameService.isStarted());

        ElectionMonitorInfo electionMonitorInfo = new ElectionMonitorInfo();
        boolean electionStarted = electionService instanceof Online ? ((Online) electionService).isStarted() : true;
        electionMonitorInfo.setStarted(electionStarted);

        brokerMonitorInfo.getReplication().setStarted(electionStarted);

        brokerMonitorInfo.setStore(storeMonitorInfo);
        brokerMonitorInfo.setNameServer(nameServerMonitorInfo);
        brokerMonitorInfo.setElection(electionMonitorInfo);

        brokerMonitorInfo.setBufferPoolMonitorInfo(storeService.monitorInfo());
        return brokerMonitorInfo;
    }


    @Override
    public BrokerStatExt getExtendBrokerStat(long timeStamp) {
       BrokerStatExt statExt=new BrokerStatExt(brokerStat);
       statExt.setTimeStamp(timeStamp);

        getJVMState(); // update current jvm state
        JVMStat recentJvmStat= statExt.getBrokerStat().getJvmStat().getRecentSnapshot();
        // update snapshot jvm memory state
        recentJvmStat.setMemoryStat(statExt.getBrokerStat().getJvmStat().getMemoryStat());

       Map<String, TopicPendingStat>  topicPendingStatMap=statExt.getTopicPendingStatMap();
       Map<String, TopicStat>  topicStatMap=brokerStat.getTopicStats();
       Map<String,ConsumerPendingStat> consumerPendingStatMap;
       Map<Integer,PartitionGroupPendingStat> partitionGroupPendingStatMap;
       Map<Short,Long> partitionPendStatMap;
       TopicPendingStat topicPendingStat;
       ConsumerPendingStat consumerPendingStat;
       PartitionGroupPendingStat partitionGroupPendingStat;
       Integer partitionGroupId;
       Consumer consumer;
       Long pending;
       Long topicPending=0L;
       Long consumerPending=0L;
       Long partitionGroupPending=0L;
       try {
       for( TopicStat topicStat :topicStatMap.values()){
            topicPendingStat=new TopicPendingStat();
            topicPendingStat.setTopic(topicStat.getTopic());
            topicPendingStatMap.put(topicStat.getTopic(),topicPendingStat);
            for(String app:topicStat.getAppStats().keySet()){
                     consumer=new Consumer(topicStat.getTopic(),app);
                     consumerPendingStat=new ConsumerPendingStat();
                     consumerPendingStat.setApp(app);
                     consumerPendingStat.setTopic(topicStat.getTopic());
                     consumerPendingStatMap=topicPendingStat.getPendingStatSubMap();
                     consumerPendingStatMap.put(app,consumerPendingStat);
                     partitionGroupPendingStatMap=consumerPendingStat.getPendingStatSubMap();
                    StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(consumer.getTopic());
                    for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
                         partitionGroupId=partitionGroupMetric.getPartitionGroup();
                         partitionGroupPendingStat=new PartitionGroupPendingStat();
                         partitionGroupPendingStat.setPartitionGroup(partitionGroupId);
                         partitionGroupPendingStat.setTopic(topicStat.getTopic());
                         partitionGroupPendingStat.setApp(app);
                         partitionGroupPendingStatMap.put(partitionGroupId,partitionGroupPendingStat);
                        for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                                if (!clusterManager.isLeader(topicStat.getTopic(), partitionMetric.getPartition())) {
                                    continue;
                                }
                                long ackIndex = consume.getAckIndex(consumer, partitionMetric.getPartition());
                                if (ackIndex < 0) {
                                    ackIndex = 0;
                                }
                                pending=partitionMetric.getRightIndex() - ackIndex;
                                partitionPendStatMap=partitionGroupPendingStat.getPendingStatSubMap();
                                partitionPendStatMap.put(partitionMetric.getPartition(),pending);
                                partitionGroupPending+=pending;
                            }
                          partitionGroupPendingStat.setPending(partitionGroupPending);
                          consumerPending+=partitionGroupPending;
                          partitionGroupPending=0L; //clear
                    }
                    consumerPendingStat.setPending(consumerPending);
                    topicPending+=consumerPending;
                    consumerPending=0L;// clear
            }
           topicPendingStat.setPending(topicPending);
           topicPending=0L; //clear
          }
        } catch (Exception e) {
            logger.info("bug",e);
            throw new MonitorException(e);
        }
        // replicas lag state
        snapshotReplicaLag();
        // runtime memory usage state
        runtimeMemoryUsageState(statExt);
        return statExt;
    }

    /**
     *  Replica log max position snapshots
     *
     **/
    public void snapshotReplicaLag(){
        Map<String, TopicStat>  topicStatMap=brokerStat.getTopicStats();
        for(TopicStat topicStat:topicStatMap.values()){
            Map<Integer,PartitionGroupStat> partitionGroupStatMap= topicStat.getPartitionGroupStatMap();
            for(PartitionGroupStat partitionGroupStat:partitionGroupStatMap.values()){
                StoreManagementService.PartitionGroupMetric partitionGroupMetric=storeManagementService.partitionGroupMetric(partitionGroupStat.getTopic(),partitionGroupStat.getPartitionGroup());
                partitionGroupStat.getReplicationStat().setMaxLogPosition(partitionGroupMetric.getRightPosition());
            }
        }
    }

    @Override
    public BrokerStartupInfo getStartInfo() {
        return brokerStartupInfo;
    }

    /**
    * fill heap and non-heap memory usage state of current
    *
    **/
   public void runtimeMemoryUsageState(BrokerStatExt brokerStatExt){
       MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
       brokerStatExt.setHeap(memoryMXBean.getHeapMemoryUsage());
       brokerStatExt.setNonHeap(memoryMXBean.getNonHeapMemoryUsage());
   }

    @Override
    public JVMStat getJVMState() {
        JVMStat jvmStat=brokerStat.getJvmStat();
        jvmStat.setMemoryStat(jvmMonitorService.memSnapshot());
        return jvmStat;
    }


    @Override
    public void addGcEventListener(GCEventListener listener) {
        this.gcNotificationParser.addListener(listener);
    }

    /**
     *  GC event listener
     *
     **/
    public class DefaultGCEventListener implements GCEventListener{

        private JVMStat jvmStat;
        public DefaultGCEventListener(JVMStat jvmStat){
            this.jvmStat=jvmStat;
        }
        @Override
        public void handleNotification(GCEvent event) {
            GcInfo gcInfo=event.getGcInfo().getGcInfo()   ;
            if(event.getType() == GCEventType.END_OF_MAJOR||event.getType() == GCEventType.END_OF_MINOR){
                jvmStat.getTotalGcTime().addAndGet(gcInfo.getDuration());
                jvmStat.getTotalGcTimes().incrementAndGet();
            }
            if(event.getType() == GCEventType.END_OF_MAJOR){
                jvmStat.getOldGcTimes().mark(gcInfo.getDuration(),1);
            }else if(event.getType()== GCEventType.END_OF_MINOR){
                jvmStat.getEdenGcTimes().mark(gcInfo.getDuration(),1);
            }
            //System.out.println(String.format("old %d   , young %d ",jvmStat.getOldGcTimes().getCount(),jvmStat.getEdenGcTimes().getCount()));
        }
    }

}