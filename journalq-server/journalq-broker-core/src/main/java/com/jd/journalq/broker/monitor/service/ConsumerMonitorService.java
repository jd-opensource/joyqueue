package com.jd.journalq.broker.monitor.service;

import com.jd.journalq.model.Pager;
import com.jd.journalq.monitor.ConsumerMonitorInfo;
import com.jd.journalq.monitor.ConsumerPartitionGroupMonitorInfo;
import com.jd.journalq.monitor.ConsumerPartitionMonitorInfo;

import java.util.List;

/**
 * broker监控服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public interface ConsumerMonitorService {

    /**
     * 获取所有消费topic监控信息
     *
     * @return
     */
    Pager<ConsumerMonitorInfo> getConsumerInfos(int page, int pageSize);

    /**
     * 获取消费者信息
     *
     * @param topic
     * @param app
     * @return
     */
    ConsumerMonitorInfo getConsumerInfoByTopicAndApp(String topic, String app);

    /**
     * 获取消费者信息
     *
     * @param topic
     * @param app
     * @return
     */
    List<ConsumerPartitionMonitorInfo> getConsumerPartitionInfos(String topic, String app);

    /**
     * 获取消费者信息
     *
     * @param topic
     * @param app
     * @param partition
     * @return
     */
    ConsumerPartitionMonitorInfo getConsumerPartitionInfoByTopicAndApp(String topic, String app, short partition);

    /**
     * 获取消费者信息
     *
     * @param topic
     * @param app
     * @return
     */
    List<ConsumerPartitionGroupMonitorInfo> getConsumerPartitionGroupInfos(String topic, String app);

    /**
     * 获取消费者信息
     *
     * @param topic
     * @param app
     * @param partitionGroupId
     * @return
     */
    ConsumerPartitionGroupMonitorInfo getConsumerPartitionGroupInfoByTopicAndApp(String topic, String app, int partitionGroupId);

}