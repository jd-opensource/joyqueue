package com.jd.journalq.handler.routing.command.config;

import com.jd.journalq.handler.binder.annotation.Page;
import com.jd.journalq.handler.binder.annotation.ParamterValue;
import com.jd.journalq.handler.binder.annotation.Path;
import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.routing.command.NsrCommandSupport;
import com.jd.journalq.handler.Constants;
import com.jd.journalq.model.domain.Config;
import com.jd.journalq.model.query.QConfig;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.service.ConfigService;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

/**
 * Created by wangxiaofei1 on 2018/10/17.
 */
public class ConfigCommand extends NsrCommandSupport<Config,ConfigService,QConfig> {

    @Override
    @Path("search")
    public Response pageQuery(@Page(typeindex = 2)QPageQuery<QConfig> qPageQuery) throws Exception {
        if (qPageQuery.getQuery()== null) {
            qPageQuery.setQuery(new QConfig());
        }
        return super.pageQuery(qPageQuery);
    }
    @Override
    @Path("delete")
    public Response delete(@ParamterValue(Constants.ID) Object id) throws Exception {
        Config newModel = service.findById(id.toString());
        int count = service.delete(newModel);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        //publish(); 暂不进行发布消息
        return Responses.success();
    }

}
