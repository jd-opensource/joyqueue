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
package com.jd.joyqueue.broker.manage.service;

import com.jd.joyqueue.monitor.BrokerMessageInfo;

import java.util.List;

/**
 * MessageManageService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public interface MessageManageService {

    /**
     * 获取message
     *
     * @param topic
     * @param app
     * @param partition
     * @param index
     * @param count
     * @return
     */
    List<BrokerMessageInfo> getPartitionMessage(String topic, String app, short partition, long index, int count);

    /**
     * 获取积压message
     *
     * @param topic
     * @param app
     * @param count
     * @return
     */
    List<BrokerMessageInfo> getPendingMessage(String topic, String app, int count);

    /**
     * 获取最后message
     *
     * @param topic
     * @param app
     * @param count
     * @return
     */
    List<BrokerMessageInfo> getLastMessage(String topic, String app, int count);

    /**
     * 获取message，如果有积压消息返回积压，否则返回最后几条
     *
     * @param topic
     * @param app
     * @param count
     * @return
     */
    List<BrokerMessageInfo> viewMessage(String topic, String app, int count);
}