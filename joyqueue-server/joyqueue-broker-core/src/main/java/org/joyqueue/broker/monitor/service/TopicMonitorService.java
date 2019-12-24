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
package org.joyqueue.broker.monitor.service;

import org.joyqueue.model.Pager;
import org.joyqueue.monitor.TopicMonitorInfo;

import java.util.List;

/**
 * broker监控服务
 *
 * author: gaohaoxiang
 * date: 2018/10/10
 */
public interface TopicMonitorService {

    /**
     * 获取所有主题的监控信息
     *
     * @param page 页数
     * @param pageSize 每页大小
     * @return 分页的主题监控信息
     */
    Pager<TopicMonitorInfo> getTopicInfos(int page, int pageSize);

    /**
     * 获取主题的监控信息
     *
     * @param topic 主题
     * @return 主题监控信息
     */
    TopicMonitorInfo getTopicInfoByTopic(String topic);

    /**
     * 获取多个列表的监控信息
     *
     * @param topics 主题列表
     * @return 主题监控信息
     */
    List<TopicMonitorInfo> getTopicInfoByTopics(List<String> topics);
}