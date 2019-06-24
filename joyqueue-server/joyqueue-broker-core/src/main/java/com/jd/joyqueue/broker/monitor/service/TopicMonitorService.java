/**
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
package com.jd.joyqueue.broker.monitor.service;

import com.jd.joyqueue.model.Pager;
import com.jd.joyqueue.monitor.TopicMonitorInfo;

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