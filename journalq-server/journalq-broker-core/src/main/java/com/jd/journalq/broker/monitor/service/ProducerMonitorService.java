package com.jd.journalq.broker.monitor.service;

import com.jd.journalq.common.model.Pager;
import com.jd.journalq.common.monitor.ProducerMonitorInfo;
import com.jd.journalq.common.monitor.ProducerPartitionGroupMonitorInfo;
import com.jd.journalq.common.monitor.ProducerPartitionMonitorInfo;

import java.util.List;

/**
 * broker监控服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public interface ProducerMonitorService {

    /**
     * 获取所有生产topic监控信息
     *
     * @return
     */
    Pager<ProducerMonitorInfo> getProduceInfos(int page, int pageSize);

    /**
     * 获取生产者信息
     *
     * @param topic
     * @param app
     * @return
     */
    ProducerMonitorInfo getProducerInfoByTopicAndApp(String topic, String app);

    /**
     * 获取生产者信息
     *
     * @param topic
     * @param app
     * @return
     */
    List<ProducerPartitionMonitorInfo> getProducerPartitionInfos(String topic, String app);

    /**
     * 获取生产者信息
     *
     * @param topic
     * @param app
     * @param partition partition
     * @return
     */
    ProducerPartitionMonitorInfo getProducerPartitionInfoByTopicAndApp(String topic, String app, short partition);

    /**
     * 获取生产者信息
     *
     * @param topic
     * @param app
     * @return
     */
    List<ProducerPartitionGroupMonitorInfo> getProducerPartitionGroupInfos(String topic, String app);

    /**
     * 获取生产者信息
     *
     * @param topic
     * @param app
     * @param partitionGroupId
     * @return
     */
    ProducerPartitionGroupMonitorInfo getProducerPartitionGroupInfoByTopicAndApp(String topic, String app, int partitionGroupId);
}