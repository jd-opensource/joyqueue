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
package io.chubao.joyqueue.broker.monitor.service.support;

import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.consumer.Consume;
import io.chubao.joyqueue.broker.election.ElectionService;
import io.chubao.joyqueue.broker.monitor.converter.BrokerMonitorConverter;
import io.chubao.joyqueue.broker.monitor.exception.MonitorException;
import io.chubao.joyqueue.broker.monitor.service.BrokerMonitorInternalService;
import io.chubao.joyqueue.monitor.BrokerMonitorInfo;
import io.chubao.joyqueue.monitor.BrokerStartupInfo;
import io.chubao.joyqueue.monitor.ElectionMonitorInfo;
import io.chubao.joyqueue.monitor.NameServerMonitorInfo;
import io.chubao.joyqueue.monitor.StoreMonitorInfo;
import io.chubao.joyqueue.network.session.Consumer;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.broker.monitor.stat.BrokerStat;
import io.chubao.joyqueue.broker.monitor.stat.BrokerStatExt;
import io.chubao.joyqueue.broker.monitor.stat.ConsumerPendingStat;
import io.chubao.joyqueue.broker.monitor.stat.PartitionGroupPendingStat;
import io.chubao.joyqueue.broker.monitor.stat.TopicPendingStat;
import io.chubao.joyqueue.broker.monitor.stat.TopicStat;
import io.chubao.joyqueue.store.StoreManagementService;
import io.chubao.joyqueue.store.StoreService;
import io.chubao.joyqueue.toolkit.format.Format;
import io.chubao.joyqueue.toolkit.lang.Online;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Map;

/**
 * BrokerMonitorInternalService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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
        runtimeMemoryUsageState(statExt);
        return statExt;
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
}