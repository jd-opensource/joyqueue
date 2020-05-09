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
package org.joyqueue.handler.routing.command.broker;

import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.exception.ValidationException;
import org.joyqueue.handler.error.ErrorCode;
import org.joyqueue.handler.routing.command.CommandSupport;
import org.joyqueue.handler.Constants;
import org.joyqueue.model.Pagination;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.Broker;
import org.joyqueue.model.domain.BrokerGroup;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.query.QBrokerGroup;
import org.joyqueue.service.BrokerGroupService;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

import java.util.List;

import java.util.stream.Collectors;


/**
 * 分组 处理器
 * Created by chenyanying3 on 2018-10-18.
 */
public class BrokerGroupCommand extends CommandSupport<BrokerGroup, BrokerGroupService, QBrokerGroup> {

    @Path("findAll")
    public Response findAll() throws Exception {
        QBrokerGroup qBrokerGroup = new QBrokerGroup();
        qBrokerGroup.setRole(session.getRole());
        return Responses.success(service.findAll(qBrokerGroup));
    }

    @Path("updateBroker")
    public Response updateBroker(@QueryParam(Constants.ID) String id, @Body Broker model) throws Exception {
        try {
            service.updateBroker(model);
        } catch (ValidationException e) {
            return Responses.error(ErrorCode.ValidationError.getCode(), e.getStatus(), e.getMessage());
        }
        return Responses.success();
    }

    @Path("mvBatchBrokerGroup")
    public Response mvBatchBrokerGroup(@QueryParam("group") String group,@Body List<Broker> brokers) throws Exception {
        QBrokerGroup qBrokerGroup = new QBrokerGroup();
        qBrokerGroup.setCode(group);
        QPageQuery<QBrokerGroup> pageQuery = new QPageQuery<>();
        Pagination pagination = new Pagination();
        pagination.setSize(1000);
        pagination.setPage(1);
        pageQuery.setPagination(pagination);
        pageQuery.setQuery(qBrokerGroup);
        Response response = this.pageQuery(pageQuery);
        List<BrokerGroup> brokerGroups = (List<BrokerGroup>) response.getData();
        BrokerGroup brokerGroup ;
        if (CollectionUtils.isNotEmpty(brokerGroups)) {
            List<BrokerGroup> collect = brokerGroups.stream().filter(bg -> bg.getCode().equals(group)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
                brokerGroup = collect.get(0);
            } else {
                return Responses.error(404,"can't find the brokerGroup:"+group);
            }
        } else {
            return Responses.error(404,"can't find the brokerGroup:"+group);
        }
        for (Broker broker: brokers) {
            broker.setGroup(new Identity(brokerGroup.getId(), brokerGroup.getCode()));
            service.updateBroker(broker);
        }
        return Responses.success();
    }
}
