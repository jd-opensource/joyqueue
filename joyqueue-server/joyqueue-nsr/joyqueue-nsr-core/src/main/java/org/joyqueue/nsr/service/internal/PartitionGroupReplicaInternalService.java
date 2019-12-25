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


import org.joyqueue.domain.Replica;
import org.joyqueue.domain.TopicName;

import java.util.List;


/**
 * @author lixiaobin6
 * 下午3:11 2018/8/13
 */
public interface PartitionGroupReplicaInternalService {

    /**
     * 根据ID获取
     *
     * @param id
     * @return
     */
    Replica getById(String id);

    /**
     * 根据Topic查找
     ** @param topic
     * @return
     */
    List<Replica> getByTopic(TopicName topic);

    /**
     * 根据Topic和PartitionGroup查找
     *
\     * @param topic
     * @param groupNo
     * @return
     */
    List<Replica> getByTopicAndGroup(TopicName topic, int groupNo);

    /**
     * 根据BrokerId查找
     *
     * @param brokerId
     * @return
     */
    List<Replica> getByBrokerId(Integer brokerId);

    /**
     * 查询全部
     * @return
     */
    List<Replica> getAll();

    /**
     * 添加
     *
     * @param replica
     */
    Replica add(Replica replica);

    /**
     * 根据ID更新
     *
     * @param replica
     */
    Replica update(Replica replica);

    /**
     * 根据id删除
     *
     * @param id
     */
    void delete(String id);
}
