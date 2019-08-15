package io.chubao.joyqueue.broker.monitor.service;

import io.chubao.joyqueue.monitor.ConnectionMonitorDetailInfo;
import io.chubao.joyqueue.monitor.ConnectionMonitorInfo;

/**
 * broker监控服务
 *
 * author: gaohaoxiang
 * date: 2018/10/10
 */
public interface ConnectionMonitorService {

    /**
     * 获取连接数监控信息
     *
     * @return 连接数监控信息
     */
    ConnectionMonitorInfo getConnectionInfo();

    /**
     * 获取主题连接数监控信息
     *
     * @param topic 主题
     * @return 连接数监控信息
     */
    ConnectionMonitorInfo getConnectionInfoByTopic(String topic);

    /**
     * 获取主题下应用的连接数监控信息
     *
     * @param topic 主题
     * @param app   应用
     * @return 连接数监控信息
     */
    ConnectionMonitorInfo getConnectionInfoByTopicAndApp(String topic, String app);

    /**
     * 获取连接详细监控信息
     *
     * @return 连接详细监控信息
     */
    ConnectionMonitorDetailInfo getConnectionDetailInfo();

    /**
     * 获取主题连接详细监控信息
     *
     * @param topic 主题
     * @return 连接详细监控信息
     */
    ConnectionMonitorDetailInfo getConnectionDetailInfoByTopic(String topic);

    /**
     * 获取主题下应用连接详细监控信息
     *
     * @param topic 主题
     * @param app   应用
     * @return 连接详细监控信息
     */
    ConnectionMonitorDetailInfo getConnectionDetailInfoByTopicAndApp(String topic, String app);

    /**
     * 获取主题消费者连接详细监控信息
     *
     * @param topic 主题
     * @return 连接详细监控信息
     */
    ConnectionMonitorDetailInfo getConsumerConnectionDetailInfoByTopic(String topic);

    /**
     * 获取主题下应用的消费者连接详细监控信息
     *
     * @param topic 主题
     * @param app   应用
     * @return 连接详细监控信息
     */
    ConnectionMonitorDetailInfo getConsumerConnectionDetailInfoByTopicAndApp(String topic, String app);

    /**
     * 获取主题生产者详细监控信息
     *
     * @param topic 主题
     * @return 连接详细监控信息
     */
    ConnectionMonitorDetailInfo getProducerConnectionDetailInfoByTopic(String topic);

    /**
     * 获取主题下应用的生产者连接详细监控信息
     *
     * @param topic 主题
     * @param app   应用
     * @return 连接详细监控信息
     */
    ConnectionMonitorDetailInfo getProducerConnectionDetailInfoByTopicAndApp(String topic, String app);
}