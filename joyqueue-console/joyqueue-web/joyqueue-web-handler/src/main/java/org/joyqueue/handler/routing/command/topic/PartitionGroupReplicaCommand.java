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
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.handler.annotation.PageQuery;
import org.joyqueue.handler.error.ConfigException;
import org.joyqueue.handler.routing.command.NsrCommandSupport;
import org.joyqueue.model.ListQuery;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.BrokerGroup;
import org.joyqueue.model.domain.TopicPartitionGroup;
import org.joyqueue.model.domain.Broker;
import org.joyqueue.model.domain.BrokerGroupRelated;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.PartitionGroupReplica;
import org.joyqueue.model.query.QBroker;
import org.joyqueue.model.query.QBrokerGroup;
import org.joyqueue.model.query.QBrokerGroupRelated;
import org.joyqueue.model.query.QPartitionGroupReplica;
import org.joyqueue.service.BrokerService;
import org.joyqueue.service.BrokerGroupService;
import org.joyqueue.service.BrokerGroupRelatedService;
import org.joyqueue.service.PartitionGroupReplicaService;
import org.joyqueue.service.TopicPartitionGroupService;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 主题队列-Broker分组 处理器
 * Created by wylixiaobin on 2018-10-19
 */
public class PartitionGroupReplicaCommand extends NsrCommandSupport<PartitionGroupReplica, PartitionGroupReplicaService, QPartitionGroupReplica> {
    @Value(nullable = false)
    private BrokerService brokerService;
    @Value(nullable = false)
    private TopicPartitionGroupService topicPartitionGroupService;
    @Value
    private BrokerGroupService brokerGroupService;
    @Value
    private BrokerGroupRelatedService brokerGroupRelatedService;

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
        QBroker qBroker2 = new QBroker();
        // 其实如果brokerGroups.size()>0 则brokerGroups.size()===1
        if (brokerGroup!=null) {
            ListQuery<QBrokerGroupRelated> brokerGroupRelatedListQuery = new ListQuery<>();
            QBrokerGroupRelated brokerGroupRelated = new QBrokerGroupRelated();
            brokerGroupRelated.setGroup(new Identity(brokerGroup));
            brokerGroupRelatedListQuery.setQuery(brokerGroupRelated);
            List<BrokerGroupRelated> brokerGroupRelateds = brokerGroupRelatedService.findByQuery(brokerGroupRelatedListQuery);
            qBroker2.setInBrokerIds(brokerGroupRelateds.stream().map(related -> (int)related.getId()).collect(Collectors.toList()));
        }
        qPageQuery.getQuery().setKeyword("");
        QPartitionGroupReplica query = qPageQuery.getQuery();
        List<PartitionGroupReplica> partitionGroupReplicas = service.getByTopicAndGroup(query.getTopic().getCode(), query.getNamespace().getCode(), query.getGroupNo());
        // 若没有输入broker分组，则继续执行
        QPageQuery<QBroker> brokerQuery2 = new QPageQuery(qPageQuery.getPagination(), qBroker2);
        brokerQuery2.getQuery().setKeyword(qPageQuery.getQuery().getKeyword());
        PageResult<Broker> brokerPage2 = brokerService.search(brokerQuery2);
        List<Broker> brokers = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(partitionGroupReplicas)) {
            for (Broker broker : brokerPage2.getResult()) {
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
        } else {
            brokers.addAll(brokerPage2.getResult());
        }
        return Responses.success(brokerPage2.getPagination(), brokers);
    }

    @Path("searchBrokerToScale")
    public Response toScaleSearch(@PageQuery QPageQuery<QPartitionGroupReplica> qPageQuery) throws Exception {
        QPartitionGroupReplica query = qPageQuery.getQuery();
        List<PartitionGroupReplica> partitionGroupReplicas = service.getByTopicAndGroup(query.getTopic().getCode(), query.getNamespace().getCode(), query.getGroupNo());
        List<BrokerGroup> brokerGroups = null;
        if (query.getTopic().getBrokerGroup() != null) {
            QBrokerGroup brokerGroup = new QBrokerGroup();
            brokerGroup.setCode(query.getTopic().getBrokerGroup().getCode());
            brokerGroups = brokerGroupService.findAll(brokerGroup).stream().filter(group -> StringUtils.containsIgnoreCase(group.getCode(),brokerGroup.getCode())).collect(Collectors.toList());
        }
        QBroker qBroker = new QBroker();
        if (brokerGroups!=null && brokerGroups.size()>0) {
            ListQuery<QBrokerGroupRelated> brokerGroupRelatedListQuery = new ListQuery<>();
            List<BrokerGroupRelated> brokerGroupRelateds = new ArrayList<>();
            for (BrokerGroup brokerGroup: brokerGroups){
                QBrokerGroupRelated brokerGroupRelated = new QBrokerGroupRelated();
                brokerGroupRelated.setGroup(new Identity(brokerGroup.getId(),brokerGroup.getCode()));
                brokerGroupRelatedListQuery.setQuery(brokerGroupRelated);
                brokerGroupRelateds.addAll(brokerGroupRelatedService.findByQuery(brokerGroupRelatedListQuery));
            }
            qBroker.setInBrokerIds(brokerGroupRelateds.stream().map(brokerGroupRelated -> (int)brokerGroupRelated.getId()).collect(Collectors.toList()));
        }
        // 如果输入broker分组,brokerGroups.size()还是为0，说明库里没有，则直接返回空数据
        else if (brokerGroups != null && query.getTopic().getBrokerGroup() != null) {
            return Responses.success();
        }
        // 若没有输入broker分组，则继续执行
        QPageQuery<QBroker> brokerQuery = new QPageQuery(qPageQuery.getPagination(), qBroker);
        brokerQuery.getQuery().setKeyword(qPageQuery.getQuery().getKeyword());
        PageResult<Broker> brokerPage = brokerService.search(brokerQuery);
        List<Broker> brokers = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(partitionGroupReplicas)) {
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
        } else {
            brokers.addAll(brokerPage.getResult());
        }
        return Responses.success(brokerPage.getPagination(), brokers);
    }


    @Path("searchBrokerToAddNew")
    public Response toAddNewPartitionGroupSearch(@PageQuery QPageQuery<QPartitionGroupReplica> qPageQuery) throws Exception {
        QPageQuery<QBroker> brokerQuery = new QPageQuery(qPageQuery.getPagination(), new QBroker());
        PageResult<Broker> brokerPage = brokerService.search(brokerQuery);
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
}
