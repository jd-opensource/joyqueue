package com.jd.journalq.nsr.service;

import com.jd.journalq.domain.Consumer;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.nsr.model.ConsumerQuery;

import java.util.List;

/**
 * @author lixiaobin6
 * 下午3:11 2018/8/13
 */
public interface ConsumerService extends DataService<Consumer, ConsumerQuery, String> {

    /**
     * 根据Topic,APP删除消费者
     *
     * @param topic
     * @param app
     */
    void deleteByTopicAndApp(TopicName topic, String app);

    /**
     * 根据topic,app获取消费者
     *
     * @param topic
     * @param app
     * @return
     */
    Consumer getByTopicAndApp(TopicName topic, String app);

    /**
     * 根据topic获取消费者
     *
     * @param topic
     * @param withConfig
     * @return
     */
    List<Consumer> getByTopic(TopicName topic, boolean withConfig);

    /**
     * 根据APP获取消费者
     *
     * @param app
     * @param withConfig
     * @return
     */
    List<Consumer> getByApp(String app, boolean withConfig);

    /**
     * 根据客户端类型获取消费者
     *
     * @param clientType
     * @return
     */
    List<Consumer> getConsumerByClientType(byte clientType);

    /**
     * 添加消费者
     *
     * @param consumer
     */
    void add(Consumer consumer);

    /**
     * 更新消费者
     *
     * @param consumer
     */

    void update(Consumer consumer);

    /**
     * 删除消费者
     *
     * @param consumer
     */
    void remove(Consumer consumer);
}
