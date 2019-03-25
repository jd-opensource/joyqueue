package com.jd.journalq.handler.routing.command.broker;

import com.jd.journalq.handler.binder.annotation.Path;
import com.jd.journalq.handler.routing.command.CommandSupport;
import com.jd.journalq.model.domain.BrokerGroup;
import com.jd.journalq.model.query.QBrokerGroup;
import com.jd.journalq.service.BrokerGroupService;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

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
}
