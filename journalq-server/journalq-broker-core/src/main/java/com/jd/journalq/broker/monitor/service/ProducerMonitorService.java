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

import com.jd.journalq.model.Pager;
import com.jd.journalq.monitor.ProducerMonitorInfo;
import com.jd.journalq.monitor.ProducerPartitionGroupMonitorInfo;
import com.jd.journalq.monitor.ProducerPartitionMonitorInfo;

import java.util.List;

/**
 * broker监控服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public interface ProducerMonitorService {

    /**
     * 获取所有生产topic监控信息
     *
     * @return
     */
    Pager<ProducerMonitorInfo> getProduceInfos(int page, int pageSize);

    /**
     * 获取生产者信息
     *
     * @param topic
     * @param app
     * @return
     */
    ProducerMonitorInfo getProducerInfoByTopicAndApp(String topic, String app);

    /**
     * 获取生产者信息
     *
     * @param topic
     * @param app
     * @return
     */
    List<ProducerPartitionMonitorInfo> getProducerPartitionInfos(String topic, String app);

    /**
     * 获取生产者信息
     *
     * @param topic
     * @param app
     * @param partition partition
     * @return
     */
    ProducerPartitionMonitorInfo getProducerPartitionInfoByTopicAndApp(String topic, String app, short partition);

    /**
     * 获取生产者信息
     *
     * @param topic
     * @param app
     * @return
     */
    List<ProducerPartitionGroupMonitorInfo> getProducerPartitionGroupInfos(String topic, String app);

    /**
     * 获取生产者信息
     *
     * @param topic
     * @param app
     * @param partitionGroupId
     * @return
     */
    ProducerPartitionGroupMonitorInfo getProducerPartitionGroupInfoByTopicAndApp(String topic, String app, int partitionGroupId);
}