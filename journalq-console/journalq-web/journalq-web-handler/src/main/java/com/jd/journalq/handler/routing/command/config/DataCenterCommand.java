package com.jd.journalq.handler.routing.command.config;

import com.jd.journalq.handler.binder.annotation.ParamterValue;
import com.jd.journalq.handler.binder.annotation.Path;
import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.routing.command.NsrCommandSupport;
import com.jd.journalq.handler.Constants;
import com.jd.journalq.model.domain.DataCenter;
import com.jd.journalq.model.query.QDataCenter;
import com.jd.journalq.service.DataCenterService;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

/**
 * Created by wangxiaofei1 on 2018/10/19.
 */
public class DataCenterCommand extends NsrCommandSupport<DataCenter,DataCenterService,QDataCenter> {

    private static final String group = "dataCenter";


    @Path("findAll")
    public Response findAll() throws Exception {
        return Responses.success(service.findByQuery(new QDataCenter()));
    }

    @Override
    @Path("delete")
    public Response delete(@ParamterValue(Constants.ID) Object id) throws Exception {
        DataCenter newModel = service.findById(id.toString());
        int count = service.delete(newModel);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        return Responses.success();
    }

}
