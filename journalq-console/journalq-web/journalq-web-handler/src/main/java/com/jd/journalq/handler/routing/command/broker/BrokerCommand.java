package com.jd.journalq.handler.routing.command.broker;

import com.jd.journalq.handler.binder.annotation.ParamterValue;
import com.jd.journalq.handler.binder.annotation.Path;
import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.routing.command.NsrCommandSupport;
import com.jd.journalq.handler.Constants;
import com.jd.journalq.model.domain.Broker;
import com.jd.journalq.model.query.QBroker;
import com.jd.journalq.service.BrokerService;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

import static com.jd.journalq.handler.Constants.ID;

/**
 * @author wylixiaobin
 * Date: 2018/10/17
 */
public class BrokerCommand extends NsrCommandSupport<Broker,BrokerService,QBroker> {


    @Override
    @Path("delete")
    public Response delete(@ParamterValue(ID) Object id) throws Exception {
        Broker newModel = service.findById(Long.valueOf(id.toString()));
        int count = service.delete(newModel);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        //publish(); 暂不进行发布消息
        return Responses.success();
    }

    @Path("get")
    public Response get(@ParamterValue(ID) Object id) throws Exception {
        Broker newModel = service.findById(Long.valueOf(id.toString()));
        if (newModel == null) {
            throw new ConfigException(getErrorCode());
        }
        return Responses.success(newModel);
    }
}
