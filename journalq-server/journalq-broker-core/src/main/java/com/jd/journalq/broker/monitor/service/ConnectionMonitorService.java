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

import com.jd.journalq.monitor.ConnectionMonitorDetailInfo;
import com.jd.journalq.monitor.ConnectionMonitorInfo;

/**
 * broker监控服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public interface ConnectionMonitorService {

    /**
     * 获取当前连接数信息
     *
     * @return
     */
    ConnectionMonitorInfo getConnectionInfo();

    /**
     * 获取当前连接数信息
     *
     * @param topic 主题
     * @return 当前生产者数量
     */
    ConnectionMonitorInfo getConnectionInfoByTopic(String topic);

    /**
     * 获取当前连接数信息
     *
     * @param topic 主题
     * @param app   应用
     * @return 当前生产者数量
     */
    ConnectionMonitorInfo getConnectionInfoByTopicAndApp(String topic, String app);

    /**
     * 返回当前所有连接数
     *
     * @return
     */
    ConnectionMonitorDetailInfo getConnectionDetailInfo();

    /**
     * 获取连接明细
     *
     * @param topic 主题
     */
    ConnectionMonitorDetailInfo getConnectionDetailInfoByTopic(String topic);

    /**
     * 获取连接明细
     *
     * @param topic 主题
     * @param app   应用
     */
    ConnectionMonitorDetailInfo getConnectionDetailInfoByTopicAndApp(String topic, String app);

    /**
     * 获取连接明细
     *
     * @param topic 主题
     */
    ConnectionMonitorDetailInfo getConsumerConnectionDetailInfoByTopic(String topic);

    /**
     * 获取连接明细
     *
     * @param topic 主题
     * @param app   应用
     */
    ConnectionMonitorDetailInfo getConsumerConnectionDetailInfoByTopicAndApp(String topic, String app);

    /**
     * 获取连接明细
     *
     * @param topic 主题
     */
    ConnectionMonitorDetailInfo getProducerConnectionDetailInfoByTopic(String topic);

    /**
     * 获取连接明细
     *
     * @param topic 主题
     * @param app   应用
     */
    ConnectionMonitorDetailInfo getProducerConnectionDetailInfoByTopicAndApp(String topic, String app);
}