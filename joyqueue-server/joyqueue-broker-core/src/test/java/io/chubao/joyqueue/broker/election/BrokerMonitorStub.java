package io.chubao.joyqueue.broker.election;

import io.chubao.joyqueue.broker.monitor.BrokerMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhuduohui on 2018/12/6.
 */
public class BrokerMonitorStub extends BrokerMonitor {
    private static Logger logger = LoggerFactory.getLogger(BrokerMonitorStub.class);

    @Override
    public void onReplicateMessage(String topic, int partitionGroup, long count, long size, double time) {
        logger.debug("Monitor replicate message of topic {} partition group {}, " +
                "count is {}, size is {}, time is {}",
                topic, partitionGroup, count, size, time);
    }

    @Override
    public void onAppendReplicateMessage(String topic, int partitionGroup, long count, long size, double time) {
        logger.debug("Monitor append replicate message of topic {} partition group {}, " +
                "count is {}, size is {}, time is {}",
                topic, partitionGroup, count, size, time);
    }
}
