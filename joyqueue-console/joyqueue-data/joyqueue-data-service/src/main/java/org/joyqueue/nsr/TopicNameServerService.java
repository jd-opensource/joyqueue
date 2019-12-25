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
package org.joyqueue.nsr;

import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.Topic;
import org.joyqueue.model.domain.TopicPartitionGroup;
import org.joyqueue.model.query.QTopic;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/1/2.
 */
public interface TopicNameServerService extends NsrService<Topic, String> {
    /**
     * 添加主题
     * @param
     * @param topic
     * @param partitionGroups
     * @
     */
    String addTopic(Topic topic, List<TopicPartitionGroup> partitionGroups) ;
    /**
     * 删除主题
     * @param
     * @param topic
     * @
     */
    int removeTopic(Topic topic) ;

    /**
     * 添加partitionGroup
     * @param partitionGroups
     * @
     */
    String addPartitionGroup(TopicPartitionGroup partitionGroups) ;
    /**
     * 移除partitionGroup
     * @
     */
    String removePartitionGroup(TopicPartitionGroup group) ;
    /**
     * 添加partitionGroup
     * @param partitionGroups
     * @
     */
    List<Integer> updatePartitionGroup(TopicPartitionGroup partitionGroups);

    /**
     * leader改变
     * @param group
     */
    int leaderChange(TopicPartitionGroup group);
    /**
     * 查找Master
     * @param replicas
     * @return
     * @
     */
    List<PartitionGroup> findPartitionGroupMaster(List<TopicPartitionGroup> replicas);

    /**
     * 查询未订阅的topic
     * @param query
     * @return
     */
    PageResult<Topic> findUnsubscribedByQuery(QPageQuery<QTopic> query);

    /**
     * 查询
     * @param query
     * @return
     */
    PageResult<Topic> search(QPageQuery<QTopic> query);

    /**
     * 根据code查询
     * @param code
     * @param namespaceCode
     * @return
     */
    Topic findByCode(@Param("namespaceCode") String namespaceCode,@Param("code")String code);

    Topic findById(String id);
}
