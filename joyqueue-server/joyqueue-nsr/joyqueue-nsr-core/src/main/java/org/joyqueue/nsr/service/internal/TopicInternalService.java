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


import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Topic;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.nsr.model.TopicQuery;

import java.util.Collection;
import java.util.List;

/**
 * @author  lixiaobin6
 * 下午3:11 2018/8/13
 */
public interface TopicInternalService {

    /**
     * 根据id查询
     * @param id
     * @return
     */
    Topic getById(String id);

    /**
     * 根据code查询
     * @param namespace
     * @param topic
     * @return
     */
    Topic getTopicByCode(String namespace, String topic);

    /**
     * 分页查询
     *
     * @param pageQuery
     * @return
     */
    PageResult<Topic> search(QPageQuery<TopicQuery> pageQuery);

    /**
     * 查询未订阅消息
     * @param pageQuery
     * @return
     */
    PageResult<Topic> findUnsubscribedByQuery(QPageQuery<TopicQuery> pageQuery);

    /**
     * 查询所有主题
     * @return
     */
    List<Topic> getAll();


    /**
     * 新增topic
     * 1.发送给所有相关broker，新添PartitionGroup命令
     * 2.碰到失败,回滚,发送给所有相关broker,删除partitionGroup命令(失败则失败，不做任何处理)
     * 数据库最终结果(成功，则topic创建，失败，则topic创建失败(如果回滚失败，则数据库不存在topic,但是存储和选举处都会有相关信息，用户生产消费不受影响，因为获取不到该topic相关元数据信息))
     *
     * @param topic topic
     * @return
     */
    void addTopic(Topic topic, List<PartitionGroup> partitionGroups);

    /**
     * 删除topic
     * 1.发送该给所有相关的broker,删除partitionGroup命令
     * 2.碰到失败，无需回滚操作
     * 数据库最终结果(成功，存储和选举可能会有异常不同步数据，用户使用不受影响，因为已经获取不到topic元数据信息)
     *
     * @param topic topic
     * @return
     */
    void removeTopic(Topic topic);

    /**
     * 新增partitionGroup
     * 1.发送给所有相关broker，新添PartitionGroup命令
     * 2.碰到失败,回滚,发送给所有相关broker,删除partitionGroup命令(失败则失败，不做任何处理)
     * 数据库最终结果(成功，则partitionGroup创建，失败，则topic创建失败(如果回滚失败，则数据库不存在topic,但是存储和选举处都会有相关信息，选举通知nsr选举结果部分需要注意,用户生产消费不受影响，因为获取不到该topic下面的partitionGroup相关元数据信息))
     *
     * @param group
     * @return
     */
    void addPartitionGroup(PartitionGroup group);

    /**
     * 删除partitionGroup
     * 1.发送该给所有相关的broker,删除partitionGroup命令
     * 2.碰到失败，无需回滚
     * 数据库最终结果(成功，存储和选举可能会有异常不同步数据，用户使用不受影响，因为已经获取不到topic该partitionGroup元数据信息)
     *
     * @param group
     * @return
     */
    void removePartitionGroup(PartitionGroup group);

    /**
     * 更新partitionGroup(保证执行成功或者失败后客户端根据获取到的元数据是可以正常消费和生产的)
     * 一.更新partitions
     * 1.通知所有的broker节点，调用存储的rePartition接口(有一个失败则认为失败)
     * 2.，失败，则回滚,调用存储 rePartition接口，失败则不处理
     * 3.数据库最终结果(成功，则成功，失败则失败，没通知成功的节点数据会和数据库不一致，用户生产和消费可能会出问题)
     * 二.添加broker节点(节点数量必须是偶数)
     * 1.第一轮,第一个节点添加 通知所有的broker节点，如果是老的节点,调用选举接口和存储接口告知该partitionGroup新增加了一个节点，如果是新增的那个节点，则调用选举接口和存储接口告知该broker上新添加了一个partitionGroup(半数通知成功，则认为成功,否则失败)
     * 2.第二轮,第二个节点添加 通知所有的broker节点，如果是老的节点,调用选举接口和存储接口告知该partitionGroup新增加了一个节点，如果是新增的那个节点，则调用选举接口和存储接口告知该broker上新添加了一个partitionGroup(半数通知成功，则认为成功，否则失败)
     * 3.第一步失败，无需回滚，直接失败,如果第二步失败,则需要回滚第一步，第一步回滚时候，如果是老的节点，调用选举接口和存储接口告知该partitionGroup删除了一个节点，如果是新增加的那个节点，则调用选举接口和存储接口告知该broker上面删除一个partitionGroup(失败则失败)
     * 4.数据库最终结果(成功，则成功，失败则失败，没通知成功的节点数据会和数据库不一致,如果该过程总触发选举，则用户生产和消费会有影响)
     * )
     * 三:删除broker节点(节点数必须是偶数)
     * 1.第一轮,第一个节点删除 通知所有的broker节点 ，如果是老的节点，调用选举接口和存储接口告知该partitionGroup删除了一个节点，如果是被删除的那个节点，则调用选举接口和存储接口告知该broker上面删除一个partitionGroup(半数通知成功，则认为成功)
     * 2.第二轮,第二个节点删除 通知所有的broker节点 ，如果是老的节点，调用选举接口和存储接口告知该partitionGroup删除了一个节点，如果是被删除的那个节点，则调用选举接口和存储接口告知该broker上面删除一个partitionGroup(半数通知成功，则认为成功)
     * 3.第一步失败，无需回滚,直接失败,如果第二步失败，则需要回滚第一步,第一步回滚时候，如果是老的节点，调用选举接口和存储接口告知该partitionGroup新增加了一个节点，如果是被删除的那个节点，则调用选举接口和存储接口告知该broker上面新增加一个partitionGroup(失败则失败)
     * 4.数据库最终结果(成功，则成功，失败则失败，没通知成功的节点数据会和数据库不一致,如果该过程总触发选举，则用户生产和消费会有影响)
     *
     * @return
     */
    Collection<Integer> updatePartitionGroup(PartitionGroup group);

    /**
     * leader上报
     * @param group
     */
    void leaderReport(PartitionGroup group);

    /**
     * 指定leader
     * @param group
     */
    void leaderChange(PartitionGroup group);
    /**
     * 删除partitionGroup
     * 1.发送该给所有相关的broker,删除partitionGroup命令
     * 2.碰到失败，无需回滚
     * 数据库最终结果(成功，存储和选举可能会有异常不同步数据，用户使用不受影响，因为已经获取不到topic该partitionGroup元数据信息)
     *
     * @return
     */
    List<PartitionGroup> getPartitionGroup(String namespace, String topic, Object[] groups);

    /**
     * 添加主题
     * @param topic
     * @return
     */
    Topic add(Topic topic);

    /**
     * 更新主题
     * @param topic
     * @return
     */
    Topic update(Topic topic);
}
