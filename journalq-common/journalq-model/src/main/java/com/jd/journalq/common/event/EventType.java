package com.jd.journalq.common.event;

public enum EventType {
    /**
     * subscribe eventType
     */
    /**
     * 添加订阅
     */
    ADD_CONSUMER,
    /**
     * 取消订阅
     */
    REMOVE_CONSUMER,
    /**
     * 更新消费策略
     */
    UPDATE_CONSUMER,
    /**
     * 添加生产者
     */
    ADD_PRODUCER,
    /**
     * 取消生产者
     */
    REMOVE_PRODUCER,
    /**
     * 更新生产策略
     */
    UPDATE_PRODUCER,
    /**
     *  broker eventType
     */
    /**
     * 删除broker
     */
    REMOVE_BROKER,
    /**
     * partition eventType
     */
    /**
     * 新建topic
     */
    ADD_TOPIC,
    /**
     * 删除topic
     */
    REMOVE_TOPIC,
    /**
     * 更新topic
     */
    UPDATE_TOPIC,
    /**
     * 更新partitionGroup
     */
    UPDATE_PARTITION_GROUP,
    /**
     * 添加partitionGroup
     */
    ADD_PARTITION_GROUP,
    /**
     * 更新partitionGroup
     */
    REMOVE_PARTITION_GROUP,
    /**
     * 添加数据中心
     */
    ADD_DATACENTER,
    /**
     * 更新
     */
    UPDATE_DATACENTER,
    /**
     * 移除
     */
    REMOVE_DATACENTER,
    /**
     * 新增配置
     */
    ADD_CONFIG,
    /**
     * 更新broker配置
     */
    UPDATE_CONFIG,

    /**
     * 删除配置
     */
    REMOVE_CONFIG,

    /**
     * 更新broker
     */
    UPDATE_BROKER

}
