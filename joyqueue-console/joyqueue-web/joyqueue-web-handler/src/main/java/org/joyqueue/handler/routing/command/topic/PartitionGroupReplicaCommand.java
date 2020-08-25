/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.handler.routing.command.topic;

import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.joyqueue.handler.annotation.PageQuery;
import org.joyqueue.handler.error.ConfigException;
import org.joyqueue.handler.routing.command.NsrCommandSupport;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.BrokerGroup;
import org.joyqueue.model.domain.TopicPartitionGroup;
import org.joyqueue.model.domain.Broker;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.PartitionGroupReplica;
import org.joyqueue.model.query.QBroker;
import org.joyqueue.model.query.QPartitionGroupReplica;
import org.joyqueue.nsr.ReplicaServerService;
import org.joyqueue.service.BrokerService;
import org.joyqueue.service.BrokerGroupService;
import org.joyqueue.service.BrokerGroupRelatedService;
import org.joyqueue.service.PartitionGroupReplicaService;
import org.joyqueue.service.TopicPartitionGroupService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 主题队列-Broker分组 处理器
 * Created by wylixiaobin on 2018-10-19
 */
public class PartitionGroupReplicaCommand extends NsrCommandSupport<PartitionGroupReplica, PartitionGroupReplicaService, QPartitionGroupReplica> {

    private static final Logger logger = LoggerFactory.getLogger(PartitionGroupReplicaCommand.class);

    @Value(nullable = false)
    private BrokerService brokerService;
    @Value(nullable = false)
    private TopicPartitionGroupService topicPartitionGroupService;
    @Value
    private BrokerGroupService brokerGroupService;
    @Value
    private BrokerGroupRelatedService brokerGroupRelatedService;
    @Value
    protected ReplicaServerService replicaServerService;

    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QPartitionGroupReplica> qPageQuery) throws Exception {
        List<PartitionGroupReplica> partitionGroupReplicas = new LinkedList<>();
        QPartitionGroupReplica query = qPageQuery.getQuery();

        if (query.getTopic() != null) {
            List<PartitionGroupReplica> queryResult = service.getByTopicAndGroup(query.getTopic().getCode(), query.getNamespace().getCode(), query.getGroupNo());
            if (queryResult != null) {
                for (PartitionGroupReplica partitionGroupReplica : queryResult) {
                    Broker broker = brokerService.findById(partitionGroupReplica.getBrokerId());
                    if (broker != null) {
                        partitionGroupReplica.setBroker(broker);
                        partitionGroupReplicas.add(partitionGroupReplica);
                    }
                }
            }
        }

        Pagination pagination = qPageQuery.getPagination();
        pagination.setTotalRecord(partitionGroupReplicas.size());

        PageResult<PartitionGroupReplica> result = new PageResult();
        result.setPagination(pagination);
        result.setResult(partitionGroupReplicas);
        return Responses.success(result.getPagination(), result.getResult());
    }

    /**
     * 返回broker默认分组的数据
     * @param qPageQuery
     * @return
     * @throws Exception
     */
    @Path("searchBrokerToScaleDefault")
    public Response toScaleDefaultSearch(@PageQuery QPageQuery<QPartitionGroupReplica> qPageQuery) throws Exception {
        QBroker qBroker = new QBroker();
        QPageQuery<QBroker> brokerQuery = new QPageQuery(qPageQuery.getPagination(), qBroker);
        brokerQuery.getQuery().setKeyword(qPageQuery.getQuery().getKeyword());
        PageResult<Broker> brokerPage = brokerService.search(brokerQuery);
        String brokerGroup = null;
        if (CollectionUtils.isNotEmpty(brokerPage.getResult())) {
            if (brokerPage.getResult().get(0).getGroup()!=null) {
                brokerGroup = brokerPage.getResult().get(0).getGroup().getCode();
            }
        }
        if (brokerGroup!=null) {
            qPageQuery.getQuery().getTopic().setBrokerGroup(new BrokerGroup(brokerGroup));
        }
        qPageQuery.getQuery().setKeyword("");
        Response response = toScaleSearch(qPageQuery);
        List<Broker> brokers = (List<Broker>) response.getData();
        if (brokers.size()==0) {
            qPageQuery.getQuery().getTopic().setBrokerGroup(null);
            return toScaleSearch(qPageQuery);
        }
        return response;
    }

    @Path("searchBrokerToScale")
    public Response toScaleSearch(@PageQuery QPageQuery<QPartitionGroupReplica> qPageQuery) throws Exception {
        QPartitionGroupReplica query = qPageQuery.getQuery();
        List<PartitionGroupReplica> partitionGroupReplicas = service.getByTopicAndGroup(query.getTopic().getCode(), query.getNamespace().getCode(), query.getGroupNo());
        QBroker qBroker = new QBroker();
        if (query.getTopic().getBrokerGroup() != null) {
            qBroker.setGroup(new Identity(query.getTopic().getBrokerGroup().getCode()));
        }
        // 若没有输入broker分组，则继续执行
        QPageQuery<QBroker> brokerQuery = new QPageQuery(qPageQuery.getPagination(), qBroker);
        brokerQuery.getQuery().setKeyword(qPageQuery.getQuery().getKeyword());
        PageResult<Broker> brokerPage = brokerService.search(brokerQuery);
        List<Broker> brokers = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(partitionGroupReplicas)) {
            if (CollectionUtils.isNotEmpty(brokerPage.getResult())) {
                for (Broker broker : brokerPage.getResult()) {
                    boolean isMatch = false;
                    for (PartitionGroupReplica partitionGroupReplica : partitionGroupReplicas) {
                        if (partitionGroupReplica.getBrokerId() == broker.getId()) {
                            isMatch = true;
                            break;
                        }
                    }
                    if (!isMatch) {
                        brokers.add(broker);
                    }
                }
            }
        } else {
            if (brokerPage.getResult()!=null) {
                brokers.addAll(brokerPage.getResult());
            }
        }
        return Responses.success(brokerPage.getPagination(), brokers);
    }

    @Path("searchBrokerToAddNewDefault")
    public Response toAddNewPartitionGroupDefaultSearch(@PageQuery QPageQuery<QPartitionGroupReplica> qPageQuery) throws Exception {
        QBroker qBroker = new QBroker();
        QPageQuery<QBroker> brokerQuery = new QPageQuery(qPageQuery.getPagination(), qBroker);
        brokerQuery.getQuery().setKeyword(qPageQuery.getQuery().getKeyword());
        PageResult<Broker> brokerPage = brokerService.search(brokerQuery);
        String brokerGroup = null;
        if (CollectionUtils.isNotEmpty(brokerPage.getResult())) {
            if (brokerPage.getResult().get(0).getGroup()!=null) {
                brokerGroup = brokerPage.getResult().get(0).getGroup().getCode();
            }
        }
        if (brokerGroup!=null) {
            qPageQuery.getQuery().getTopic().setBrokerGroup(new BrokerGroup(brokerGroup));
        }
        qPageQuery.getQuery().setKeyword("");
        Response response = toAddNewPartitionGroupSearch(qPageQuery);
        List<Broker> brokers = (List<Broker>) response.getData();
        if (brokers.size()==0) {
            qPageQuery.getQuery().getTopic().setBrokerGroup(null);
            return toAddNewPartitionGroupSearch(qPageQuery);
        }
        return response;
    }

    @Path("searchBrokerToAddNew")
    public Response toAddNewPartitionGroupSearch(@PageQuery QPageQuery<QPartitionGroupReplica> qPageQuery) throws Exception {
        QPartitionGroupReplica query = qPageQuery.getQuery();
        QBroker qBroker = new QBroker();
        if (query.getTopic().getBrokerGroup() != null) {
            qBroker.setGroup(new Identity(query.getTopic().getBrokerGroup().getCode()));
        }
        // 若没有输入broker分组，则继续执行
        QPageQuery<QBroker> brokerQuery = new QPageQuery(qPageQuery.getPagination(), qBroker);
        brokerQuery.getQuery().setKeyword(qPageQuery.getQuery().getKeyword());
        PageResult<Broker> brokerPage = brokerService.search(brokerQuery);
        if (CollectionUtils.isEmpty(brokerPage.getResult())) {
            brokerPage.setResult(Collections.emptyList());
        }
        return Responses.success(brokerPage.getPagination(), brokerPage.getResult());
    }

    @Override
    @Path("add")
    public Response add(@Body PartitionGroupReplica model) throws Exception {
        TopicPartitionGroup group = topicPartitionGroupService.findByTopicAndGroup(model.getNamespace().getCode(),
                model.getTopic().getCode(), model.getGroupNo());
        if (group.getElectType().equals(TopicPartitionGroup.ElectType.raft.type())) {
            model.setRole(PartitionGroupReplica.ROLE_DYNAMIC);
        } else model.setRole(PartitionGroupReplica.ROLE_SLAVE);
        int count = service.addWithNameservice(model, group);
        if (count <= 0) {
            throw new ConfigException(addErrorCode());
        }
        return Responses.success(model);
    }

    @Path("delete")
    public Response delete(@Body PartitionGroupReplica partitionGroupReplica) throws Exception {
        PartitionGroupReplica replica = service.findById(partitionGroupReplica.getId());
        int count = service.removeWithNameservice(replica, topicPartitionGroupService.findByTopicAndGroup(
                replica.getNamespace().getCode(), replica.getTopic().getCode(), replica.getGroupNo()));
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
        if (count <= 0) {
            throw new ConfigException(updateErrorCode());
        }
        return Responses.success();
    }

    @Path("findPartitionGroupReplica")
    public Response findPartitionGroupReplica(@QueryParam("brokerId") Integer brokerId) {
        try {
            List<PartitionGroupReplica> partitionGroupReplica = replicaServerService.findPartitionGroupReplica(brokerId);
            return Responses.success(partitionGroupReplica);
        } catch (Exception e) {
            logger.error("", e);
            return Responses.error(500, e.getMessage());
        }
    }
}
