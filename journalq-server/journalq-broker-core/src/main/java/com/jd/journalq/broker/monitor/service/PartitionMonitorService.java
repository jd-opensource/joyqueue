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
package com.jd.journalq.broker.monitor.service;

import com.jd.journalq.monitor.PartitionGroupMonitorInfo;
import com.jd.journalq.monitor.PartitionMonitorInfo;

import java.util.List;

/**
 * broker监控服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public interface PartitionMonitorService {

     /**
     * 获取partition监控信息
     *
     * @param topic     主题
     * @param partition partition
     * @return
     */
    PartitionMonitorInfo getPartitionInfoByTopic(String topic, short partition);

    /**
     * 获取partition监控信息
     *
     * @param topic
     * @return
     */
    List<PartitionMonitorInfo> getPartitionInfosByTopic(String topic);

    /**
     * 获取partition监控信息
     *
     * @param topic     主题
     * @param app       应用
     * @param partition partition
     * @return 出队流量
     */
    PartitionMonitorInfo getPartitionInfoByTopicAndApp(String topic, String app, short partition);

    /**
     * 获取partition监控信息
     *
     * @param topic
     * @param app
     * @return
     */
    List<PartitionMonitorInfo> getPartitionInfosByTopicAndApp(String topic, String app);

    /**
     * 获取partitionGroup监控信息
     *
     * @param topic          主题
     * @param partitionGroup partitionGroup
     * @return 出队流量
     */
    PartitionGroupMonitorInfo getPartitionGroupInfoByTopic(String topic, int partitionGroup);

    /**
     * 获取partitionGroup监控信息
     *
     * @param topic
     * @return
     */
    List<PartitionGroupMonitorInfo> getPartitionGroupInfosByTopic(String topic);

    /**
     * 获取partitionGroup监控信息
     *
     * @param topic          主题
     * @param app            应用
     * @param partitionGroup partitionGroup
     * @return 出队流量
     */
    PartitionGroupMonitorInfo getPartitionGroupInfoByTopicAndApp(String topic, String app, int partitionGroup);

    /**
     * 获取partitionGroup监控信息
     *
     * @param topic
     * @param app
     * @return
     */
    List<PartitionGroupMonitorInfo> getPartitionGroupInfosByTopicAndApp(String topic, String app);

}