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
package io.chubao.joyqueue.nsr.service;


import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.model.ReplicaQuery;

import java.util.List;


/**
 * @author lixiaobin6
 * 下午3:11 2018/8/13
 */
public interface PartitionGroupReplicaService extends DataService<Replica, ReplicaQuery, String> {
    /**
     * 根据Topic删除
     *
     * @param topic
     */
    void deleteByTopic(TopicName topic);

    /**
     * 根据partitionGroup删除
     *
     * @param topic
     * @param groupNo
     */
    void deleteByTopicAndPartitionGroup(TopicName topic, int groupNo);

    /**
     * 根据Topic查找
     ** @param topic
     * @return
     */
    List<Replica> findByTopic(TopicName topic);

    /**
     * 根据Topic和PartitionGroup查找
     *
\     * @param topic
     * @param groupNo
     * @return
     */
    List<Replica> findByTopicAndGrPartitionGroup(TopicName topic, int groupNo);

    /**
     * 根据BrokerId查找
     *
     * @param brokerId
     * @return
     */
    List<Replica> findByBrokerId(Integer brokerId);
}
