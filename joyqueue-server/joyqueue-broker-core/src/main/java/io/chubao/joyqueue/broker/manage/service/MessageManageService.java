package io.chubao.joyqueue.broker.manage.service;

import io.chubao.joyqueue.monitor.BrokerMessageInfo;

import java.util.List;

/**
 * MessageManageService
 *
 * author: gaohaoxiang
 * date: 2018/10/15
 */
public interface MessageManageService {

    /**
     * 获取主题下应用分区的消息
     *
     * @param topic 主题
     * @param app 应用
     * @param partition 分区
     * @param index 索引
     * @param count 数量
     * @return 消息列表
     */
    List<BrokerMessageInfo> getPartitionMessage(String topic, String app, short partition, long index, int count);

    /**
     * 获取主题下应用的积压消息
     *
     * @param topic 主题
     * @param app 应用
     * @param count 数量
     * @return 消息列表
     */
    List<BrokerMessageInfo> getPendingMessage(String topic, String app, int count);

    /**
     * 获取主题下应用的最新消息
     *
     * @param topic 主题
     * @param app 应用
     * @param count 数量
     * @return 消息列表
     */
    List<BrokerMessageInfo> getLastMessage(String topic, String app, int count);

    /**
     * 获取主题下应用的消息，如果有积压返回积压，否则返回最新几条
     *
     * @param topic 主题
     * @param app 应用
     * @param count 数量
     * @return 消息列表
     */
    List<BrokerMessageInfo> viewMessage(String topic, String app, int count);
}