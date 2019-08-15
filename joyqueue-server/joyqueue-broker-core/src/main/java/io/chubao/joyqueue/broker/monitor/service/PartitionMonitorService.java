package io.chubao.joyqueue.broker.monitor.service;

import io.chubao.joyqueue.monitor.PartitionGroupMonitorInfo;
import io.chubao.joyqueue.monitor.PartitionMonitorInfo;

import java.util.List;

/**
 * broker监控服务
 *
 * author: gaohaoxiang
 * date: 2018/10/10
 */
public interface PartitionMonitorService {

     /**
     * 获取分区监控信息
     *
     * @param topic     主题
     * @param partition 分区
     * @return 分区监控信息
     */
    PartitionMonitorInfo getPartitionInfoByTopic(String topic, short partition);

    /**
     * 获取主题下所有分区的监控信息
     *
     * @param topic 主题
     * @return 分区监控信息列表
     */
    List<PartitionMonitorInfo> getPartitionInfosByTopic(String topic);

    /**
     * 获取主题下应用分区的监控信息
     *
     * @param topic     主题
     * @param app       应用
     * @param partition 分区
     * @return 分区监控信息
     */
    PartitionMonitorInfo getPartitionInfoByTopicAndApp(String topic, String app, short partition);

    /**
     * 获取主题下应用的所有分区的监控信息
     *
     * @param topic 主题
     * @param app 应用
     * @return 分区监控信息列表
     */
    List<PartitionMonitorInfo> getPartitionInfosByTopicAndApp(String topic, String app);

    /**
     * 获取主题下分区组的监控信息
     *
     * @param topic          主题
     * @param partitionGroup 分区组
     * @return 分组取监控信息
     */
    PartitionGroupMonitorInfo getPartitionGroupInfoByTopic(String topic, int partitionGroup);

    /**
     * 获取主题的所有分区组监控信息
     *
     * @param topic 主题
     * @return 分区组监控信息列表
     */
    List<PartitionGroupMonitorInfo> getPartitionGroupInfosByTopic(String topic);

    /**
     * 获取主题下应用分区组的监控信息
     *
     * @param topic          主题
     * @param app            应用
     * @param partitionGroup 分区组
     * @return 分区组监控信息
     */
    PartitionGroupMonitorInfo getPartitionGroupInfoByTopicAndApp(String topic, String app, int partitionGroup);

    /**
     * 获取主题下应用所有分区组的监控信息
     *
     * @param topic 主题
     * @param app 应用
     * @return 分区组监控信息列表
     */
    List<PartitionGroupMonitorInfo> getPartitionGroupInfosByTopicAndApp(String topic, String app);

}