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
package io.chubao.joyqueue.service;

import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.AppUnsubscribedTopic;
import io.chubao.joyqueue.model.domain.Broker;
import io.chubao.joyqueue.model.domain.BrokerGroup;
import io.chubao.joyqueue.model.domain.Identity;
import io.chubao.joyqueue.model.domain.Topic;
import io.chubao.joyqueue.model.query.QTopic;
import io.chubao.joyqueue.nsr.NsrService;

import java.util.List;

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

    PageResult<Topic> search(QPageQuery<QTopic> query);

}
