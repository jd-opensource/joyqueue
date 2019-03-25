package com.jd.journalq.nsr.service;


import com.jd.journalq.common.domain.Producer;
import com.jd.journalq.common.domain.TopicName;
import com.jd.journalq.nsr.model.ProducerQuery;

import java.util.List;

/**
 * @author lixiaobin6
 * 下午3:11 2018/8/13
 */
public interface ProducerService  extends DataService<Producer, ProducerQuery, String> {
    /**
     * 根据Topic，APP删除
     *
     * @param topic
     * @param app
     */
    void deleteByTopicAndApp(TopicName topic, String app);

    /**
     * 根据Topic和APP查找
     *
     * @param topic
     * @param app
     * @return
     */
    Producer getByTopicAndApp(TopicName topic, String app);

    /**
     * 根据Topic查找
     *
     * @param topic
     * @param withConfig
     * @return
     */
    List<Producer> getByTopic(TopicName topic, boolean withConfig);

    /**
     * 根据APP查找
     *
     * @param app
     * @param withConfig
     * @return
     */
    List<Producer> getByApp(String app, boolean withConfig);

    /**
     * 添加
     *
     * @param producer
     */
    void add(Producer producer);

    /**
     * 更新
     *
     * @param producer
     */
    void update(Producer producer);

    /**
     * 删除
     *
     * @param producer
     */
    void remove(Producer producer);

    /**
     * 根据客户端类型获取生产者
     *
     * @param clientType
     * @return
     */
    List<Producer> getProducerByClientType(byte clientType);}

