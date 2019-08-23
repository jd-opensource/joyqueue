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
package io.chubao.joyqueue.nsr;

import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.Topic;
import io.chubao.joyqueue.model.domain.TopicPartitionGroup;
import io.chubao.joyqueue.model.query.QTopic;
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
     * @throws Exception
     */
    String addTopic(Topic topic, List<TopicPartitionGroup> partitionGroups) throws Exception;
    /**
     * 删除主题
     * @param
     * @param topic
     * @throws Exception
     */
    int removeTopic(Topic topic) throws Exception;

    /**
     * 添加partitionGroup
     * @param partitionGroups
     * @throws Exception
     */
    String addPartitionGroup(TopicPartitionGroup partitionGroups) throws Exception;
    /**
     * 移除partitionGroup
     * @throws Exception
     */
    String removePartitionGroup(TopicPartitionGroup group) throws Exception;
    /**
     * 添加partitionGroup
     * @param partitionGroups
     * @throws Exception
     */
    List<Integer> updatePartitionGroup(TopicPartitionGroup partitionGroups) throws Exception;

    /**
     * leader改变
     * @param group
     */
    int leaderChange(TopicPartitionGroup group);
    /**
     * 查找Master
     * @param replicas
     * @return
     * @throws Exception
     */
    List<PartitionGroup> findPartitionGroupMaster(List<TopicPartitionGroup> replicas) throws Exception;

    /**
     * 查询未订阅的topic
     * @param query
     * @return
     */
    PageResult<Topic> findUnsubscribedByQuery(QPageQuery<QTopic> query) throws Exception;

    /**
     * 查询
     * @param query
     * @return
     */
    PageResult<Topic> search(QPageQuery<QTopic> query) throws Exception;

    /**
     * 根据code查询
     * @param code
     * @param namespaceCode
     * @return
     */
    Topic findByCode(@Param("namespaceCode") String namespaceCode,@Param("code")String code);

    Topic findById(String id);
}
