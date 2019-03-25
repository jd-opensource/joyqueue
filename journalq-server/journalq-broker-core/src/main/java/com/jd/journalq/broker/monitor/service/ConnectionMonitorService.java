package com.jd.journalq.broker.monitor.service;

import com.jd.journalq.common.monitor.ConnectionMonitorDetailInfo;
import com.jd.journalq.common.monitor.ConnectionMonitorInfo;

/**
 * broker监控服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public interface ConnectionMonitorService {

    /**
     * 获取当前连接数信息
     *
     * @return
     */
    ConnectionMonitorInfo getConnectionInfo();

    /**
     * 获取当前连接数信息
     *
     * @param topic 主题
     * @return 当前生产者数量
     */
    ConnectionMonitorInfo getConnectionInfoByTopic(String topic);

    /**
     * 获取当前连接数信息
     *
     * @param topic 主题
     * @param app   应用
     * @return 当前生产者数量
     */
    ConnectionMonitorInfo getConnectionInfoByTopicAndApp(String topic, String app);

    /**
     * 返回当前所有连接数
     *
     * @return
     */
    ConnectionMonitorDetailInfo getConnectionDetailInfo();

    /**
     * 获取连接明细
     *
     * @param topic 主题
     */
    ConnectionMonitorDetailInfo getConnectionDetailInfoByTopic(String topic);

    /**
     * 获取连接明细
     *
     * @param topic 主题
     * @param app   应用
     */
    ConnectionMonitorDetailInfo getConnectionDetailInfoByTopicAndApp(String topic, String app);

    /**
     * 获取连接明细
     *
     * @param topic 主题
     */
    ConnectionMonitorDetailInfo getConsumerConnectionDetailInfoByTopic(String topic);

    /**
     * 获取连接明细
     *
     * @param topic 主题
     * @param app   应用
     */
    ConnectionMonitorDetailInfo getConsumerConnectionDetailInfoByTopicAndApp(String topic, String app);

    /**
     * 获取连接明细
     *
     * @param topic 主题
     */
    ConnectionMonitorDetailInfo getProducerConnectionDetailInfoByTopic(String topic);

    /**
     * 获取连接明细
     *
     * @param topic 主题
     * @param app   应用
     */
    ConnectionMonitorDetailInfo getProducerConnectionDetailInfoByTopicAndApp(String topic, String app);
}