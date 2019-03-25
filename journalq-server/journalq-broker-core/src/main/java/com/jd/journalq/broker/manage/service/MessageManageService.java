package com.jd.journalq.broker.manage.service;

import com.jd.journalq.monitor.BrokerMessageInfo;

import java.util.List;

/**
 * MessageManageService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public interface MessageManageService {

    /**
     * 获取message
     *
     * @param topic
     * @param app
     * @param partition
     * @param index
     * @param count
     * @return
     */
    List<BrokerMessageInfo> getPartitionMessage(String topic, String app, short partition, long index, int count);

    /**
     * 获取积压message
     *
     * @param topic
     * @param app
     * @param count
     * @return
     */
    List<BrokerMessageInfo> getPendingMessage(String topic, String app, int count);

    /**
     * 获取最后message
     *
     * @param topic
     * @param app
     * @param count
     * @return
     */
    List<BrokerMessageInfo> getLastMessage(String topic, String app, int count);

    /**
     * 获取message，如果有积压消息返回积压，否则返回最后几条
     *
     * @param topic
     * @param app
     * @param count
     * @return
     */
    List<BrokerMessageInfo> viewMessage(String topic, String app, int count);
}