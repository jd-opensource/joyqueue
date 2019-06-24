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
package com.jd.joyqueue.handler.routing.command.topic;

import com.google.common.primitives.Ints;
import com.jd.joyqueue.handler.annotation.PageQuery;
import com.jd.joyqueue.model.PageResult;
import com.jd.joyqueue.model.QPageQuery;
import com.jd.joyqueue.handler.error.ConfigException;
import com.jd.joyqueue.handler.routing.command.NsrCommandSupport;
import com.jd.joyqueue.model.domain.Broker;
import com.jd.joyqueue.model.domain.PartitionGroupReplica;
import com.jd.joyqueue.model.domain.TopicPartitionGroup;
import com.jd.joyqueue.model.query.QBroker;
import com.jd.joyqueue.model.query.QPartitionGroupReplica;
import com.jd.joyqueue.service.BrokerService;
import com.jd.joyqueue.service.PartitionGroupReplicaService;
import com.jd.joyqueue.service.TopicPartitionGroupService;
import com.jd.joyqueue.util.NullUtil;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

import java.util.List;

/**
 * 主题队列-Broker分组 处理器
 * Created by wylixiaobin on 2018-10-19
 */
public class PartitionGroupReplicaCommand extends NsrCommandSupport<PartitionGroupReplica, PartitionGroupReplicaService, QPartitionGroupReplica> {
    @Value(nullable = false)
    private BrokerService brokerService;
    @Value(nullable = false)
    private TopicPartitionGroupService topicPartitionGroupService;
    @Path("searchBrokerToScale")
    public Response toScaleSearch(@PageQuery QPageQuery<QPartitionGroupReplica> qPageQuery) throws Exception {
        List<PartitionGroupReplica> list = service.findByQuery(qPageQuery.getQuery());
        QPageQuery<QBroker> brokerQuery = new QPageQuery(qPageQuery.getPagination(),new QBroker());
        if (NullUtil.isNotEmpty(list)) {
            brokerQuery.getQuery().setNotInBrokerIds(Ints.asList(list.stream().mapToInt(
                    replica -> Long.valueOf(replica.getBrokerId()).intValue()).toArray()));
            Broker broker = list.get(0).getBroker();
            if (broker != null && broker.getGroup() != null) {
                brokerQuery.getQuery().setBrokerGroupId(broker.getGroup().getId());
            }
        }
        brokerQuery.getQuery().setKeyword(qPageQuery.getQuery().getKeyword());
        PageResult<Broker> result = brokerService.findByQuery(brokerQuery);
        return Responses.success(result.getPagination(), result.getResult());
    }
    @Path("searchBrokerToAddNew")
    public Response toAddNewPartitionGroupSearch(@PageQuery QPageQuery<QPartitionGroupReplica> qPageQuery) throws Exception {
        List<PartitionGroupReplica > list = service.findByQuery(qPageQuery.getQuery());
        QPageQuery<QBroker> brokerQuery = new QPageQuery(qPageQuery.getPagination(),new QBroker());
        if(null!=list && list.size()>0 && list.get(0).getBroker() != null) {
            brokerQuery.getQuery().setBrokerGroupId(list.get(0).getBroker().getGroup().getId());
        }
        PageResult<Broker> result = brokerService.findByQuery(brokerQuery);
        return Responses.success(result.getPagination(), result.getResult());
    }
    @Override
    @Path("add")
    public Response add(@Body PartitionGroupReplica model) throws Exception {
        TopicPartitionGroup group = topicPartitionGroupService.findByTopicAndGroup(model.getNamespace().getCode(),
                model.getTopic().getCode(),model.getGroupNo());
        if(group.getElectType().equals(TopicPartitionGroup.ElectType.raft.type())) {
            model.setRole(PartitionGroupReplica.ROLE_DYNAMIC);
        }
        else model.setRole(PartitionGroupReplica.ROLE_SLAVE);
        int count = service.addWithNameservice(model,group);
        if (count <= 0) {
            throw new ConfigException(addErrorCode());
        }
        return Responses.success(model);
    }

    @Path("delete")
    public Response delete(@Body PartitionGroupReplica partitionGroupReplica) throws Exception {
        PartitionGroupReplica replica = service.findById(partitionGroupReplica.getId());
        int count = service.removeWithNameservice(replica,topicPartitionGroupService.findByTopicAndGroup(
                replica.getNamespace().getCode(),replica.getTopic().getCode(),replica.getGroupNo()));
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        return Responses.success(replica);
    }
    @Path("leader")
    public Response leaderChange(@Body PartitionGroupReplica model) throws Exception {
        TopicPartitionGroup topicPartitionGroup = new TopicPartitionGroup();
        topicPartitionGroup.setTopic(model.getTopic());
        topicPartitionGroup.setNamespace(model.getNamespace());
        topicPartitionGroup.setLeader(model.getBrokerId());
        topicPartitionGroup.setGroupNo(model.getGroupNo());
        topicPartitionGroup.setOutSyncReplicas(model.getOutSyncReplicas());
        int count = topicPartitionGroupService.leaderChange(topicPartitionGroup);
        if (count<=0) {
            throw new ConfigException(updateErrorCode());
        }
        return Responses.success();
    }
}
