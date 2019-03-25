package com.jd.journalq.service;

import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.model.domain.*;
import com.jd.journalq.model.query.QTopic;
import com.jd.journalq.nsr.NsrService;

import java.util.List;

/**
 * 主题服务
 * Created by chenyanying3 on 2018-10-18.
 */
public interface TopicService extends NsrService<Topic, QTopic,String> {

    /**
     * 保存：带分组和Broker信息
     * @param topic
     * @param brokerGroup
     * @param brokers
     * @param operator 操作人
     */
    void addWithBrokerGroup(Topic topic, BrokerGroup brokerGroup, List<Broker> brokers, Identity operator);

    /**
     * 查询未订阅的topics
     * @param query
     * @return
     */
    PageResult<Topic> findUnsubscribedByQuery(QPageQuery<QTopic> query);

    /**
     * 查询某个app下未订阅的topics
     * @param query
     * @return
     */
    PageResult<AppUnsubscribedTopic> findAppUnsubscribedByQuery(QPageQuery<QTopic> query);

    /**
     * 根据topic code和namespace code查找topic
     * @param namespaceCode
     * @param code
     * @return
     */
    Topic findByCode(String namespaceCode, String code);

}
