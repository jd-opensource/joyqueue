package com.jd.journalq.broker.manage.service;

/**
 * ConnectionManageService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public interface ConnectionManageService {

    /**
     * 关闭生产者连接
     * 返回关闭的连接数
     *
     * @param topic
     * @param app
     */
    int closeProducer(String topic, String app);

    /**
     * 关闭消费者连接
     * 返回关闭的连接数
     *
     * @param topic
     * @param app
     */
    int closeConsumer(String topic, String app);
}