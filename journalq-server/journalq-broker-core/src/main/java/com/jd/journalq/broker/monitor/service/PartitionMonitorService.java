package com.jd.journalq.broker.monitor.service;

import com.jd.journalq.common.monitor.PartitionGroupMonitorInfo;
import com.jd.journalq.common.monitor.PartitionMonitorInfo;

import java.util.List;

/**
 * broker监控服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public interface PartitionMonitorService {

     /**
     * 获取partition监控信息
     *
     * @param topic     主题
     * @param partition partition
     * @return
     */
    PartitionMonitorInfo getPartitionInfoByTopic(String topic, short partition);

    /**
     * 获取partition监控信息
     *
     * @param topic
     * @return
     */
    List<PartitionMonitorInfo> getPartitionInfosByTopic(String topic);

    /**
     * 获取partition监控信息
     *
     * @param topic     主题
     * @param app       应用
     * @param partition partition
     * @return 出队流量
     */
    PartitionMonitorInfo getPartitionInfoByTopicAndApp(String topic, String app, short partition);

    /**
     * 获取partition监控信息
     *
     * @param topic
     * @param app
     * @return
     */
    List<PartitionMonitorInfo> getPartitionInfosByTopicAndApp(String topic, String app);

    /**
     * 获取partitionGroup监控信息
     *
     * @param topic          主题
     * @param partitionGroup partitionGroup
     * @return 出队流量
     */
    PartitionGroupMonitorInfo getPartitionGroupInfoByTopic(String topic, int partitionGroup);

    /**
     * 获取partitionGroup监控信息
     *
     * @param topic
     * @return
     */
    List<PartitionGroupMonitorInfo> getPartitionGroupInfosByTopic(String topic);

    /**
     * 获取partitionGroup监控信息
     *
     * @param topic          主题
     * @param app            应用
     * @param partitionGroup partitionGroup
     * @return 出队流量
     */
    PartitionGroupMonitorInfo getPartitionGroupInfoByTopicAndApp(String topic, String app, int partitionGroup);

    /**
     * 获取partitionGroup监控信息
     *
     * @param topic
     * @param app
     * @return
     */
    List<PartitionGroupMonitorInfo> getPartitionGroupInfosByTopicAndApp(String topic, String app);

}