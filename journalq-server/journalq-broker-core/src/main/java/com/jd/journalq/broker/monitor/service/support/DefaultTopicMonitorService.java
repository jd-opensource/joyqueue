package com.jd.journalq.broker.monitor.service.support;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.monitor.converter.BrokerMonitorConverter;
import com.jd.journalq.broker.monitor.service.TopicMonitorService;
import com.jd.journalq.broker.monitor.stat.BrokerStat;
import com.jd.journalq.broker.monitor.stat.TopicStat;
import com.jd.journalq.model.Pager;
import com.jd.journalq.monitor.TopicMonitorInfo;
import com.jd.journalq.store.StoreManagementService;

import java.util.List;
import java.util.Map;

/**
 * TopicMonitorService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public class DefaultTopicMonitorService implements TopicMonitorService {

    private BrokerStat brokerStat;

    public DefaultTopicMonitorService(BrokerStat brokerStat) {
        this.brokerStat = brokerStat;
    }

    @Override
    public Pager<TopicMonitorInfo> getTopicInfos(int page, int pageSize) {
        int total = brokerStat.getTopicStats().size();
        int startIndex = (page - 1) * pageSize;
        int endIndex = startIndex + pageSize;
        int index = 0;
        List<TopicMonitorInfo> data = Lists.newArrayListWithCapacity(pageSize);

        for (Map.Entry<String, TopicStat> topicStatEntry : brokerStat.getTopicStats().entrySet()) {
            if (index >= startIndex && index < endIndex) {
                data.add(convertTopicMonitorInfo(topicStatEntry.getValue()));
            }
            index ++;
        }
        return new Pager<>(page, pageSize, total, data);
    }

    @Override
    public TopicMonitorInfo getTopicInfoByTopic(String topic) {
        TopicStat topicStat = brokerStat.getOrCreateTopicStat(topic);
        return convertTopicMonitorInfo(topicStat);
    }

    @Override
    public List<TopicMonitorInfo> getTopicInfoByTopics(List<String> topics) {
        List<TopicMonitorInfo> result = Lists.newArrayListWithCapacity(topics.size());
        for (String topic : topics) {
            TopicMonitorInfo topicMonitorInfo = convertTopicMonitorInfo(brokerStat.getOrCreateTopicStat(topic));
            result.add(topicMonitorInfo);
        }
        return result;
    }

    protected TopicMonitorInfo convertTopicMonitorInfo(TopicStat topicStat) {
        TopicMonitorInfo topicMonitorInfo = new TopicMonitorInfo();
        topicMonitorInfo.setTopic(topicStat.getTopic());
        topicMonitorInfo.setConnection(BrokerMonitorConverter.convertConnectionMonitorInfo(topicStat.getConnectionStat()));
        topicMonitorInfo.setEnQueue(BrokerMonitorConverter.convertEnQueueMonitorInfo(topicStat.getEnQueueStat()));
        topicMonitorInfo.setDeQueue(BrokerMonitorConverter.convertDeQueueMonitorInfo(topicStat.getDeQueueStat()));
        return topicMonitorInfo;
    }
}