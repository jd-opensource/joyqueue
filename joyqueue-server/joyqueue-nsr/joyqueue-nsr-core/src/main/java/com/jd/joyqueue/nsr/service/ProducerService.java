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
package com.jd.joyqueue.nsr.service;


import com.jd.joyqueue.domain.Producer;
import com.jd.joyqueue.domain.TopicName;
import com.jd.joyqueue.nsr.model.ProducerQuery;

import java.util.List;

/**
 * @author lixiaobin6
 * 下午3:11 2018/8/13
 */
public interface ProducerService  extends DataService<Producer, ProducerQuery, String> {
    /**
     * 根据Topic，APP删除
     *
     * @param topic
     * @param app
     */
    void deleteByTopicAndApp(TopicName topic, String app);

    /**
     * 根据Topic和APP查找
     *
     * @param topic
     * @param app
     * @return
     */
    Producer getByTopicAndApp(TopicName topic, String app);

    /**
     * 根据Topic查找
     *
     * @param topic
     * @param withConfig
     * @return
     */
    List<Producer> getByTopic(TopicName topic, boolean withConfig);

    /**
     * 根据APP查找
     *
     * @param app
     * @param withConfig
     * @return
     */
    List<Producer> getByApp(String app, boolean withConfig);

    /**
     * 添加
     *
     * @param producer
     */
    void add(Producer producer);

    /**
     * 更新
     *
     * @param producer
     */
    void update(Producer producer);

    /**
     * 删除
     *
     * @param producer
     */
    void remove(Producer producer);

    /**
     * 根据客户端类型获取生产者
     *
     * @param clientType
     * @return
     */
    List<Producer> getProducerByClientType(byte clientType);}

