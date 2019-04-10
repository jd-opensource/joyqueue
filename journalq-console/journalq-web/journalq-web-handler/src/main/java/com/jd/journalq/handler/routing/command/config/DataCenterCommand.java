package com.jd.journalq.handler.routing.command.config;


import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.routing.command.NsrCommandSupport;
import com.jd.journalq.model.domain.DataCenter;
import com.jd.journalq.model.query.QDataCenter;
import com.jd.journalq.service.DataCenterService;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

import static com.jd.journalq.handler.Constants.ID;

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
    public Response delete(@QueryParam(ID) String id) throws Exception {
        DataCenter newModel = service.findById(id);
        int count = service.delete(newModel);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        return Responses.success();
    }

}
