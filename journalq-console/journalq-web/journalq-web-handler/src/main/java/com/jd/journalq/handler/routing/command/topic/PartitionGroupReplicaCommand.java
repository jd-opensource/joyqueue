package com.jd.journalq.handler.routing.command.topic;

import com.google.common.primitives.Ints;
import com.jd.journalq.common.model.PageResult;
import com.jd.journalq.common.model.QPageQuery;
import com.jd.journalq.handler.binder.annotation.GenericBody;
import com.jd.journalq.handler.binder.annotation.GenericValue;
import com.jd.journalq.handler.binder.annotation.Page;
import com.jd.journalq.handler.binder.annotation.Path;
import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.routing.command.NsrCommandSupport;
import com.jd.journalq.model.domain.Broker;
import com.jd.journalq.model.domain.PartitionGroupReplica;
import com.jd.journalq.model.domain.TopicPartitionGroup;
import com.jd.journalq.model.query.QBroker;
import com.jd.journalq.model.query.QPartitionGroupReplica;
import com.jd.journalq.service.BrokerService;
import com.jd.journalq.service.PartitionGroupReplicaService;
import com.jd.journalq.service.TopicPartitionGroupService;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

import java.util.List;

/**
 * 主题队列-Broker分组 处理器
 * Created by wylixiaobin on 2018-10-19
 */
public class PartitionGroupReplicaCommand extends NsrCommandSupport<PartitionGroupReplica, PartitionGroupReplicaService, QPartitionGroupReplica> {
    @GenericValue
    private BrokerService brokerService;
    @GenericValue
    private TopicPartitionGroupService topicPartitionGroupService;
    @Path("searchBrokerToScale")
    public Response toScaleSearch(@Page(typeindex = 2) QPageQuery<QPartitionGroupReplica> qPageQuery) throws Exception {
        List<PartitionGroupReplica> list = service.findByQuery(qPageQuery.getQuery());
        QPageQuery<QBroker> brokerQuery = new QPageQuery(qPageQuery.getPagination(),new QBroker());
        if(null!=list && list.size()>0) {
            brokerQuery.getQuery().setNotInBrokerIds(Ints.asList(list.stream().mapToInt(
                    replica -> Long.valueOf(replica.getBrokerId()).intValue()).toArray()));
            brokerQuery.getQuery().setBrokerGroupId(list.get(0).getBroker().getGroup().getId());
        }
        PageResult<Broker> result = brokerService.findByQuery(brokerQuery);
        return Responses.success(result.getPagination(), result.getResult());
    }
    @Path("searchBrokerToAddNew")
    public Response toAddNewPartitionGroupSearch(@Page(typeindex = 2) QPageQuery<QPartitionGroupReplica> qPageQuery) throws Exception {
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
    public Response add(@GenericBody(type = GenericBody.BodyType.JSON,typeindex = 0)PartitionGroupReplica model) throws Exception {
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
    public Response delete(@GenericBody(type = GenericBody.BodyType.JSON,typeindex = 0)PartitionGroupReplica partitionGroupReplica) throws Exception {
        PartitionGroupReplica replica = service.findById(partitionGroupReplica.getId());
        int count = service.removeWithNameservice(replica,topicPartitionGroupService.findByTopicAndGroup(
                replica.getNamespace().getCode(),replica.getTopic().getCode(),replica.getGroupNo()));
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        return Responses.success(replica);
    }
    @Path("leader")
    public Response leaderChange(@GenericBody(type = GenericBody.BodyType.JSON,typeindex = 0) PartitionGroupReplica model) throws Exception {
        TopicPartitionGroup topicPartitionGroup = new TopicPartitionGroup();
        topicPartitionGroup.setTopic(model.getTopic());
        topicPartitionGroup.setNamespace(model.getNamespace());
        topicPartitionGroup.setLeader(model.getBrokerId());
        topicPartitionGroup.setGroupNo(model.getGroupNo());
        int count = topicPartitionGroupService.leaderChange(topicPartitionGroup);
        if (count<=0) {
            throw new ConfigException(updateErrorCode());
        }
        return Responses.success();
    }
}
