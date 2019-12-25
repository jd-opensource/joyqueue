/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.monitor.service.support;

import com.google.common.collect.Lists;
import org.joyqueue.broker.monitor.converter.BrokerMonitorConverter;
import org.joyqueue.broker.monitor.service.TopicMonitorService;
import org.joyqueue.broker.monitor.stat.BrokerStat;
import org.joyqueue.broker.monitor.stat.TopicStat;
import org.joyqueue.model.Pager;
import org.joyqueue.monitor.TopicMonitorInfo;
import org.joyqueue.store.StoreManagementService;

import java.util.List;

/**
 * TopicMonitorService
 *
 * author: gaohaoxiang
 * date: 2018/10/15
 */
public class DefaultTopicMonitorService implements TopicMonitorService {

    private BrokerStat brokerStat;
    private StoreManagementService storeManagementService;

    public DefaultTopicMonitorService(BrokerStat brokerStat, StoreManagementService storeManagementService) {
        this.brokerStat = brokerStat;
        this.storeManagementService = storeManagementService;
    }

    @Override
    public Pager<TopicMonitorInfo> getTopicInfos(int page, int pageSize) {
        int total = storeManagementService.storeMetrics().length;
        int startIndex = (page - 1) * pageSize;
        int endIndex = startIndex + pageSize;
        int index = 0;
        List<TopicMonitorInfo> data = Lists.newArrayListWithCapacity(pageSize);

        for (StoreManagementService.TopicMetric topicMetric : storeManagementService.storeMetrics()) {
            if (!topicMetric.isUsabled()) {
                continue;
            }
            if (index >= startIndex && index < endIndex) {
                TopicMonitorInfo topicMonitorInfo = getTopicInfoByTopic(topicMetric.getTopic());
                TopicStat topicStat = brokerStat.getOrCreateTopicStat(topicMonitorInfo.getTopic());
                data.add(convertTopicMonitorInfo(topicStat));
            }
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