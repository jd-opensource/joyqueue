package io.chubao.joyqueue.broker.monitor;

/**
 * 生产监控
 *
 * author: gaohaoxiang
 * date: 2018/10/10
 */
public interface ProducerMonitor {

    /**
     * 生产消息
     * @param topic
     * @param app
     * @param partitionGroup
     * @param partition
     * @param count
     * @param size
     * @param time
     */
    void onPutMessage(String topic, String app, int partitionGroup, short partition, long count, long size, double time);
}