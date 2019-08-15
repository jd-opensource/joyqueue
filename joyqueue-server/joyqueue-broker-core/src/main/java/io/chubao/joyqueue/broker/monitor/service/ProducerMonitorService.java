package io.chubao.joyqueue.broker.monitor.service;

import io.chubao.joyqueue.model.Pager;
import io.chubao.joyqueue.monitor.ProducerMonitorInfo;
import io.chubao.joyqueue.monitor.ProducerPartitionGroupMonitorInfo;
import io.chubao.joyqueue.monitor.ProducerPartitionMonitorInfo;

import java.util.List;

/**
 * broker监控服务
 *
 * author: gaohaoxiang
 * date: 2018/10/10
 */
public interface ProducerMonitorService {

    /**
     * 获取所有生产监控信息
     *
     * @param page 页数
     * @param pageSize 每页数量
     * @return 分页的生产监控信息
     */
    Pager<ProducerMonitorInfo> getProduceInfos(int page, int pageSize);

    /**
     * 获取主题下应用的生产监控信息
     *
     * @param topic 主题
     * @param app 应用
     * @return 生产监控信息
     */
    ProducerMonitorInfo getProducerInfoByTopicAndApp(String topic, String app);

    /**
     * 获取主题下应用所有分区的生产监控信息
     *
     * @param topic 主题
     * @param app 应用
     * @return 分区生产监控信息列表
     */
    List<ProducerPartitionMonitorInfo> getProducerPartitionInfos(String topic, String app);

    /**
     * 获取主题下应用分区的生产监控信息
     *
     * @param topic 主题
     * @param app 应用
     * @param partition 分区
     * @return 分区生产监控信息
     */
    ProducerPartitionMonitorInfo getProducerPartitionInfoByTopicAndApp(String topic, String app, short partition);

    /**
     * 获取主题下应用所有分区组的生产监控信息
     *
     * @param topic 主题
     * @param app 应用
     * @return 分区组生产监控信息列表
     */
    List<ProducerPartitionGroupMonitorInfo> getProducerPartitionGroupInfos(String topic, String app);

    /**
     * 获取主题下应用分区组的生产监控信息
     *
     * @param topic 主题
     * @param app 应用
     * @param partitionGroupId 分区组
     * @return 分区组生产监控信息
     */
    ProducerPartitionGroupMonitorInfo getProducerPartitionGroupInfoByTopicAndApp(String topic, String app, int partitionGroupId);
}