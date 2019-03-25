package com.jd.journalq.broker.monitor.service;

import com.jd.journalq.model.Pager;
import com.jd.journalq.monitor.TopicMonitorInfo;

import java.util.List;

/**
 * broker监控服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public interface TopicMonitorService {

    /**
     * 获取所有topic监控信息
     *
     * @return
     */
    Pager<TopicMonitorInfo> getTopicInfos(int page, int pageSize);

    /**
     * 获取topic监控信息
     *
     * @param topic 主题
     * @return 入队数量
     */
    TopicMonitorInfo getTopicInfoByTopic(String topic);

    /**
     * 获取topic监控信息
     *
     * @param topics 主题
     * @return 入队数量
     */
    List<TopicMonitorInfo> getTopicInfoByTopics(List<String> topics);
}