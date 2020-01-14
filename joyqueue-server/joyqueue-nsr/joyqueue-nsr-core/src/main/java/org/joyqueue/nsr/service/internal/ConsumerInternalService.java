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
package org.joyqueue.nsr.service.internal;

import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.TopicName;

import java.util.List;

/**
 * @author lixiaobin6
 * 下午3:11 2018/8/13
 */
public interface ConsumerInternalService {

    /**
     * 根据id
     * @param id
     * @return
     */
    Consumer getById(String id);

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
     * @return
     */
    List<Consumer> getByTopic(TopicName topic);

    /**
     * 根据APP获取消费者
     *
     * @param app
     * @return
     */
    List<Consumer> getByApp(String app);

    /**
     * 查询全部
     * @return
     */
    List<Consumer> getAll();

    /**
     * 添加消费者
     *
     * @param consumer
     */
    Consumer add(Consumer consumer);

    /**
     * 更新消费者
     *
     * @param consumer
     */

    Consumer update(Consumer consumer);

    /**
     * 删除消费者
     *
     * @param id
     */
    void delete(String id);
}
