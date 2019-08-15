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
package io.chubao.joyqueue.handler.routing.command.broker;

import io.chubao.joyqueue.exception.ValidationException;
import io.chubao.joyqueue.handler.error.ErrorCode;
import io.chubao.joyqueue.handler.routing.command.CommandSupport;
import io.chubao.joyqueue.model.domain.Broker;
import io.chubao.joyqueue.model.domain.BrokerGroup;
import io.chubao.joyqueue.model.query.QBrokerGroup;
import io.chubao.joyqueue.service.BrokerGroupService;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

import static io.chubao.joyqueue.handler.Constants.ID;


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
    public Response updateBroker(@QueryParam(ID) String id, @Body Broker model) throws Exception {
        try {
            service.updateBroker(model);
        } catch (ValidationException e) {
            return Responses.error(ErrorCode.ValidationError.getCode(), e.getStatus(), e.getMessage());
        }
        return Responses.success();
    }
}
