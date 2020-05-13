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
package org.joyqueue.service;

import org.joyqueue.domain.TopicName;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.AppUnsubscribedTopic;
import org.joyqueue.model.domain.Broker;
import org.joyqueue.model.domain.BrokerGroup;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.Topic;
import org.joyqueue.model.query.QTopic;
import org.joyqueue.nsr.NsrService;

import java.util.List;
import java.util.Set;

/**
 * 主题服务
 * Created by chenyanying3 on 2018-10-18.
 */
public interface TopicService extends NsrService<Topic ,String> {

    /**
     * 保存：带分组和Broker信息
     * @param topic
     * @param brokerGroup
     * @param brokers
     * @param operator 操作人
     */
    void addWithBrokerGroup(Topic topic, BrokerGroup brokerGroup, List<Broker> brokers, Identity operator);

    /**
     * 查询未订阅的topics
     * @param query
     * @return
     */
    PageResult<Topic> findUnsubscribedByQuery(QPageQuery<QTopic> query);

    /**
     * 查询某个app下未订阅的topics
     * @param query
     * @return
     */
    PageResult<AppUnsubscribedTopic> findAppUnsubscribedByQuery(QPageQuery<QTopic> query);

    /**
     * 根据topic code和namespace code查找topic
     * @param namespaceCode
     * @param code
     * @return
     */
    Topic findByCode(String namespaceCode, String code);

    /**
     * 分页查询
     * @param query
     * @return
     */
    PageResult<Topic> search(QPageQuery<QTopic> query);

    /**
     * 查询broker 上所有的 topic full name
     * @param brokerId
     * @return topic full name list
     **/
    List<TopicName> findTopic(String brokerId) throws Exception;

    /**
     * 根据opic查询producer，consumer所有相关app
     * @param namespace
     * @param topicCode
     * @return
     */
    Set<String> findAppsByTopic(String namespace, String topicCode);

}
