package com.jd.journalq.handler.routing.command.broker;

import com.jd.journalq.exception.ValidationException;
import com.jd.journalq.handler.Constants;
import com.jd.journalq.handler.binder.annotation.GenericBody;
import com.jd.journalq.handler.binder.annotation.ParamterValue;
import com.jd.journalq.handler.binder.annotation.Path;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.journalq.handler.routing.command.CommandSupport;
import com.jd.journalq.model.domain.Broker;
import com.jd.journalq.model.domain.BrokerGroup;
import com.jd.journalq.model.query.QBrokerGroup;
import com.jd.journalq.service.BrokerGroupService;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

import static com.jd.journalq.handler.Constants.ID;

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
    public Response updateBroker(@ParamterValue(Constants.ID)Object id, @GenericBody(type = GenericBody.BodyType.JSON,typeindex = 0) Broker model) throws Exception {
        try {
            service.updateBroker(model);
        } catch (ValidationException e) {
            return Responses.error(ErrorCode.ValidationError.getCode(), e.getStatus(), e.getMessage());
        }
        return Responses.success();
    }
}
