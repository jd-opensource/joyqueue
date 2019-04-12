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
package com.jd.journalq.nsr.service;

import com.jd.journalq.domain.Consumer;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.nsr.model.ConsumerQuery;

import java.util.List;

/**
 * @author lixiaobin6
 * 下午3:11 2018/8/13
 */
public interface ConsumerService extends DataService<Consumer, ConsumerQuery, String> {

    /**
     * 根据Topic,APP删除消费者
     *
     * @param topic
     * @param app
     */
    void deleteByTopicAndApp(TopicName topic, String app);

    /**
     * 根据topic,app获取消费者
     *
     * @param topic
     * @param app
     * @return
     */
    Consumer getByTopicAndApp(TopicName topic, String app);

    /**
     * 根据topic获取消费者
     *
     * @param topic
     * @param withConfig
     * @return
     */
    List<Consumer> getByTopic(TopicName topic, boolean withConfig);

    /**
     * 根据APP获取消费者
     *
     * @param app
     * @param withConfig
     * @return
     */
    List<Consumer> getByApp(String app, boolean withConfig);

    /**
     * 根据客户端类型获取消费者
     *
     * @param clientType
     * @return
     */
    List<Consumer> getConsumerByClientType(byte clientType);

    /**
     * 添加消费者
     *
     * @param consumer
     */
    void add(Consumer consumer);

    /**
     * 更新消费者
     *
     * @param consumer
     */

    void update(Consumer consumer);

    /**
     * 删除消费者
     *
     * @param consumer
     */
    void remove(Consumer consumer);
}
