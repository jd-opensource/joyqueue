package io.chubao.joyqueue.broker.monitor;

import io.chubao.joyqueue.broker.monitor.stat.AppStat;
import io.chubao.joyqueue.broker.monitor.stat.PartitionGroupStat;
import io.chubao.joyqueue.broker.monitor.stat.PartitionStat;
import io.chubao.joyqueue.broker.monitor.stat.TopicStat;
import io.chubao.joyqueue.toolkit.service.Service;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * BrokerMonitorSlicer
 *
 * author: gaohaoxiang
 * date: 2019/6/12
 */
public class BrokerMonitorSlicer extends Service implements Runnable {

    private BrokerMonitor brokerMonitor;
    private ScheduledExecutorService sliceThread;

    public BrokerMonitorSlicer(BrokerMonitor brokerMonitor) {
        this.brokerMonitor = brokerMonitor;
    }

    @Override
    protected void validate() throws Exception {
        this.sliceThread = Executors.newScheduledThreadPool(1);
    }

    @Override
    protected void doStart() throws Exception {
        this.sliceThread.scheduleWithFixedDelay(this, 0, 1000 * 60, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doStop() {
        this.sliceThread.shutdown();
    }

    @Override
    public void run() {
        for (Map.Entry<String, TopicStat> topicEntry : brokerMonitor.getBrokerStat().getTopicStats().entrySet()) {
            TopicStat topicStat = topicEntry.getValue();
            topicStat.getEnQueueStat().slice();
            topicStat.getDeQueueStat().slice();

            for (Map.Entry<Integer, PartitionGroupStat> partitionGroupStatEntry : topicStat.getPartitionGroupStatMap().entrySet()) {
                partitionGroupStatEntry.getValue().getEnQueueStat().slice();
                partitionGroupStatEntry.getValue().getDeQueueStat().slice();
                partitionGroupStatEntry.getValue().getReplicationStat().getReplicaStat().slice();
                partitionGroupStatEntry.getValue().getReplicationStat().getAppendStat().slice();

                for (Map.Entry<Short, PartitionStat> partitionStatEntry : partitionGroupStatEntry.getValue().getPartitionStatMap().entrySet()) {
                    partitionStatEntry.getValue().getEnQueueStat().slice();
                    partitionStatEntry.getValue().getDeQueueStat().slice();
                }
            }

            for (Map.Entry<String, AppStat> appEntry : topicStat.getAppStats().entrySet()) {
                // topic
                AppStat appStat = appEntry.getValue();
                appStat.getProducerStat().getEnQueueStat().slice();
                appStat.getConsumerStat().getDeQueueStat().slice();

                // app slice
                for (Map.Entry<Integer, PartitionGroupStat> partitionGroupEntry : appStat.getPartitionGroupStatMap().entrySet()) {
                    partitionGroupEntry.getValue().getEnQueueStat().slice();
                    partitionGroupEntry.getValue().getDeQueueStat().slice();
                    for (Map.Entry<Short, PartitionStat> partitionEntry : partitionGroupEntry.getValue().getPartitionStatMap().entrySet()) {
                        partitionEntry.getValue().getEnQueueStat().slice();
                        partitionEntry.getValue().getDeQueueStat().slice();
                    }
                }

                // producer slice
                for (Map.Entry<Integer, PartitionGroupStat> partitionGroupEntry : appStat.getProducerStat().getPartitionGroupStatMap().entrySet()) {
                    partitionGroupEntry.getValue().getEnQueueStat().slice();
                    partitionGroupEntry.getValue().getDeQueueStat().slice();
                    for (Map.Entry<Short, PartitionStat> partitionEntry : partitionGroupEntry.getValue().getPartitionStatMap().entrySet()) {
                        partitionEntry.getValue().getEnQueueStat().slice();
                        partitionEntry.getValue().getDeQueueStat().slice();
                    }
                }

                // consumer slice
                for (Map.Entry<Integer, PartitionGroupStat> partitionGroupEntry : appStat.getConsumerStat().getPartitionGroupStatMap().entrySet()) {
                    partitionGroupEntry.getValue().getEnQueueStat().slice();
                    partitionGroupEntry.getValue().getDeQueueStat().slice();
                    for (Map.Entry<Short, PartitionStat> partitionEntry : partitionGroupEntry.getValue().getPartitionStatMap().entrySet()) {
                        partitionEntry.getValue().getEnQueueStat().slice();
                        partitionEntry.getValue().getDeQueueStat().slice();
                    }
                }
            }
        }

        brokerMonitor.getBrokerStat().getReplicationStat().getAppendStat().slice();
        brokerMonitor.getBrokerStat().getReplicationStat().getReplicaStat().slice();

        brokerMonitor.getBrokerStat().getEnQueueStat().slice();
        brokerMonitor.getBrokerStat().getDeQueueStat().slice();
    }
}