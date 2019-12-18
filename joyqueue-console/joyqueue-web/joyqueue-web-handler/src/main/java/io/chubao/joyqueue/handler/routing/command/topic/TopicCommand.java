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
package io.chubao.joyqueue.handler.routing.command.topic;

import com.google.common.base.Preconditions;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import io.chubao.joyqueue.handler.annotation.PageQuery;
import io.chubao.joyqueue.handler.error.ConfigException;
import io.chubao.joyqueue.handler.error.ErrorCode;
import io.chubao.joyqueue.handler.routing.command.NsrCommandSupport;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.model.domain.AppUnsubscribedTopic;
import io.chubao.joyqueue.model.domain.Broker;
import io.chubao.joyqueue.model.domain.Topic;
import io.chubao.joyqueue.model.domain.User;
import io.chubao.joyqueue.model.query.QTopic;
import io.chubao.joyqueue.service.BrokerService;
import io.chubao.joyqueue.service.ConsumerService;
import io.chubao.joyqueue.service.ProducerService;
import io.chubao.joyqueue.service.TopicPartitionGroupService;
import io.chubao.joyqueue.service.TopicService;

import java.util.List;

/**
 * 主题 处理器
 * Created by chenyanying3 on 2018-10-18.
 */
public class TopicCommand extends NsrCommandSupport<Topic, TopicService, QTopic> {
    @Value(nullable = false)
    protected BrokerService brokerService;
    @Value(nullable = false)
    protected TopicPartitionGroupService topicPartitionGroupService;
    @Value(nullable = false)
    protected ConsumerService consumerService;
    @Value(nullable = false)
    protected ProducerService producerService;

    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QTopic> qPageQuery) throws Exception {
        PageResult<Topic> result = service.search(qPageQuery);
        return Responses.success(result.getPagination(), result.getResult());
    }

    @Path("addWithBrokerGroup")
    public Response addWithBrokerGroup(@Body Topic topic) throws Exception {
        //参数校验
        if (topic == null || topic.getBrokerGroup() == null || topic.getBrokers() == null || topic.getBrokers().isEmpty()) {
            new ConfigException(ErrorCode.BadRequest);
        }
        if(topic.getReplica()>topic.getBrokers().size())topic.setReplica(topic.getBrokers().size());
        service.addWithBrokerGroup(topic, topic.getBrokerGroup(), topic.getBrokers(), operator);
        return Responses.success(topic);
    }

    @Path("delete")
    public Response delete(@Body(type = Body.BodyType.TEXT) String id) throws Exception {
        return super.delete(id);
    }

    @Path("/api/mqtt/topic/add")
    public Response addTopic(@Body Topic topic) throws Exception {
        //参数校验
        if (topic == null || topic.getBrokerGroup() == null) {
            new ConfigException(ErrorCode.BadRequest);
        }
        //新建主题
        List<Broker> brokerList = brokerService.findByGroup(topic.getBrokerGroup().getId());
        Preconditions.checkArgument(null==brokerList||brokerList.size()<1,topic.getBrokerGroup().getCode()+"分组暂时无可用broker");
        topic.setBrokers(brokerList);
        if(topic.getReplica()>brokerList.size())topic.setReplica(brokerList.size());
        service.addWithBrokerGroup(topic, topic.getBrokerGroup(), topic.getBrokers(), operator);
        return Responses.success(topic);
    }

    @Path("searchUnsubscribed")
    public Response searchUnsubscribed(@PageQuery QPageQuery<QTopic> qPageQuery) throws Exception {
        QTopic qTopic = qPageQuery.getQuery();
        if (qTopic == null) {
            throw new ConfigException(ErrorCode.BadRequest);
        }
        qTopic.setUserId(Long.valueOf(String.valueOf(session.getId())));
        qTopic.setAdmin(session.getRole()== User.UserRole.ADMIN.value() ? Boolean.TRUE : Boolean.FALSE);
        qTopic.setKeyword(qTopic.getKeyword()==null?null:qTopic.getKeyword().trim());
        PageResult<AppUnsubscribedTopic> result = service.findAppUnsubscribedByQuery(qPageQuery);

        return Responses.success(result.getPagination(), result.getResult());
    }

    @Path("getById")
    public Response getById(@Body Topic model) {
        try {
            return Responses.success(service.findById(model.getId()));
        } catch (Exception e) {
            return Responses.error(e);
        }
    }

}
