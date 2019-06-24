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
import com.jd.joyqueue.monitor.ConsumerMonitorInfo;
import com.jd.joyqueue.monitor.ConsumerPartitionGroupMonitorInfo;
import com.jd.joyqueue.monitor.ConsumerPartitionMonitorInfo;

import java.util.List;

/**
 * broker监控服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public interface ConsumerMonitorService {

    /**
     * 获取所有消费topic监控信息
     *
     * @return
     */
    Pager<ConsumerMonitorInfo> getConsumerInfos(int page, int pageSize);

    /**
     * 获取消费者信息
     *
     * @param topic
     * @param app
     * @return
     */
    ConsumerMonitorInfo getConsumerInfoByTopicAndApp(String topic, String app);

    /**
     * 获取消费者信息
     *
     * @param topic
     * @param app
     * @return
     */
    List<ConsumerPartitionMonitorInfo> getConsumerPartitionInfos(String topic, String app);

    /**
     * 获取消费者信息
     *
     * @param topic
     * @param app
     * @param partition
     * @return
     */
    ConsumerPartitionMonitorInfo getConsumerPartitionInfoByTopicAndApp(String topic, String app, short partition);

    /**
     * 获取消费者信息
     *
     * @param topic
     * @param app
     * @return
     */
    List<ConsumerPartitionGroupMonitorInfo> getConsumerPartitionGroupInfos(String topic, String app);

    /**
     * 获取消费者信息
     *
     * @param topic
     * @param app
     * @param partitionGroupId
     * @return
     */
    ConsumerPartitionGroupMonitorInfo getConsumerPartitionGroupInfoByTopicAndApp(String topic, String app, int partitionGroupId);

}