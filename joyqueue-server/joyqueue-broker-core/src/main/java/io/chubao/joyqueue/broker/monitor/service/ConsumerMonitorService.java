package io.chubao.joyqueue.broker.monitor.service;

import io.chubao.joyqueue.model.Pager;
import io.chubao.joyqueue.monitor.ConsumerMonitorInfo;
import io.chubao.joyqueue.monitor.ConsumerPartitionGroupMonitorInfo;
import io.chubao.joyqueue.monitor.ConsumerPartitionMonitorInfo;

import java.util.List;

/**
 * broker监控服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public interface ConsumerMonitorService {

    /**
     * 获取所有消费监控信息
     *
     * @param page 页数
     * @param pageSize 每页数量
     * @return 分页的消费监控信息
     */
    Pager<ConsumerMonitorInfo> getConsumerInfos(int page, int pageSize);

    /**
     * 获取主题下应用的消费监控信息
     *
     * @param topic 主题
     * @param app 应用
     * @return 消费者监控信息
     */
    ConsumerMonitorInfo getConsumerInfoByTopicAndApp(String topic, String app);

    /**
     * 获取主题下应用所有分区的消费监控信息
     *
     * @param topic 主题
     * @param app 应用
     * @return 消费监控信息列表
     */
    List<ConsumerPartitionMonitorInfo> getConsumerPartitionInfos(String topic, String app);

    /**
     * 获取主题下应用分区的消费监控信息
     *
     * @param topic 主题
     * @param app 应用
     * @param partition 分区
     * @return 消费监控信息
     */
    ConsumerPartitionMonitorInfo getConsumerPartitionInfoByTopicAndApp(String topic, String app, short partition);

    /**
     * 获取主题下应用所有分区组的消费监控信息
     *
     * @param topic 主题
     * @param app 应用
     * @return 分区组消费监控信息列表
     */
    List<ConsumerPartitionGroupMonitorInfo> getConsumerPartitionGroupInfos(String topic, String app);

    /**
     * 获取主题下应用分组的消费监控信息
     *
     * @param topic 主题
     * @param app 应用
     * @param partitionGroupId 分区组
     * @return 分区组消费监控信息
     */
    ConsumerPartitionGroupMonitorInfo getConsumerPartitionGroupInfoByTopicAndApp(String topic, String app, int partitionGroupId);

}