package io.chubao.joyqueue.broker.manage.service;

import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.monitor.PartitionAckMonitorInfo;

import java.util.List;

/**
 * ConsumerManageService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public interface ConsumerManageService {

    /**
     * 设置主题下应用分区的确认位置
     *
     * @param topic 主题
     * @param app 应用
     * @param partition 分区
     * @param index 索引
     * @return 是否设置成功
     * @throws JoyQueueException
     */
    boolean setAckIndex(String topic, String app, short partition, long index) throws JoyQueueException;

    /**
     * 设置主题下应用分区的确认位置到最大
     *
     * @param topic 主题
     * @param app 应用
     * @param partition 分区
     * @return 是否设置成功
     * @throws JoyQueueException
     */
    boolean setMaxAckIndex(String topic, String app, short partition) throws JoyQueueException;

    /**
     * 获取主题下应用的确认位置
     *
     * @param topic 主题
     * @param app 应用
     * @param partition 分区
     * @return 确认位置
     */
    long getAckIndex(String topic, String app, short partition);

    /**
     * 获取主题下应用所有分区的确认位置
     *
     * @param topic 主题
     * @param app 应用
     * @return 确认位置列表
     */
    List<PartitionAckMonitorInfo> getAckIndexes(String topic, String app);

    /**
     * 设置主题下应用所有分区的确认位置到最大
     *
     * @param topic 主题
     * @param app 应用
     * @return 是否设置成功
     * @throws JoyQueueException
     */
    boolean setMaxAckIndexes(String topic, String app) throws JoyQueueException;

    /**
     * 根据时间设置主题下应用分区的确认位置
     *
     * @param topic 主题
     * @param app 应用
     * @param partition 分区
     * @param timestamp 时间戳
     * @return 是否设置成功
     * @throws JoyQueueException
     */
    boolean setAckIndexByTime(String topic, String app, short partition, long timestamp) throws JoyQueueException;

    /**
     * 根据时间获取主题下应用分区的确认位置
     *
     * @param topic 主题
     * @param app 应用
     * @param partition 分区
     * @param timestamp 时间戳
     * @return 确认位置
     */
    long getAckIndexByTime(String topic, String app, short partition, long timestamp);

    /**
     * 根据时间获取主题下所有分区的确认位置
     *
     * @param topic 主题
     * @param app 应用
     * @param timestamp 时间戳
     * @return 确认位置列表
     */
    List<PartitionAckMonitorInfo> getTopicAckIndexByTime(String topic, String app , long timestamp);

    /**
     * 根据时间设置主题下应用所有分区的确认位置
     *
     * @param topic 主题
     * @param app 应用
     * @param timestamp 时间戳
     * @return 是否设置成功
     * @throws JoyQueueException
     */
    boolean setAckIndexesByTime(String topic, String app, long timestamp) throws JoyQueueException;
}
