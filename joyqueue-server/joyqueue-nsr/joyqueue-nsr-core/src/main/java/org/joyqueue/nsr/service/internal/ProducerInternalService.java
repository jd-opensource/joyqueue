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


import org.joyqueue.domain.Producer;
import org.joyqueue.domain.TopicName;

import java.util.List;

/**
 * @author lixiaobin6
 * 下午3:11 2018/8/13
 */
public interface ProducerInternalService {

    /**
     * 根据id查找
     *
     * @param id
     * @return
     */
    Producer getById(String id);

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
     * @return
     */
    List<Producer> getByTopic(TopicName topic);

    /**
     * 根据APP查找
     *
     * @param app
     * @return
     */
    List<Producer> getByApp(String app);

    /**
     * 查询全部
     * @return
     */
    List<Producer> getAll();

    /**
     * 添加
     *
     * @param producer
     */
    Producer add(Producer producer);

    /**
     * 更新
     *
     * @param producer
     */
    Producer update(Producer producer);

    /**
     * 删除
     *
     * @param id
     */
    void delete(String id);

}
