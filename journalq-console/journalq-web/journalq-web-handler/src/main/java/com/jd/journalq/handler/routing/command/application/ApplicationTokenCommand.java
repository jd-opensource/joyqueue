package com.jd.journalq.handler.routing.command.application;


import com.jd.journalq.handler.annotation.PageQuery;
import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.journalq.handler.routing.command.NsrCommandSupport;
import com.jd.journalq.model.domain.Application;
import com.jd.journalq.model.domain.ApplicationToken;
import com.jd.journalq.model.domain.Identity;
import com.jd.journalq.model.query.QApplicationToken;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.service.ApplicationTokenService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

import static com.jd.journalq.handler.Constants.*;

/**
 * Created by wangxiaofei1 on 2018/10/23.
 */
public class ApplicationTokenCommand extends NsrCommandSupport<ApplicationToken, ApplicationTokenService, QApplicationToken> {
    @Value(APPLICATION)
    protected Application application;

    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QApplicationToken> qPageQuery) throws Exception {
        QApplicationToken query = qPageQuery.getQuery();
        query.setApplication(new Identity(application.getCode()));
        return super.pageQuery(qPageQuery);
    }

    @Path("add")
    public Response add(@Body ApplicationToken model) throws Exception {
        int tokenCount = service.countByAppId(application.getId());
        if (tokenCount >= 5) {
            throw new ConfigException(ErrorCode.ExcessiveToken);
        }
        model.setApplication(application.identity());
        service.add(model);
        return Responses.success();
    }

    /**
     * 查询按应用id
     * @param appId
     * @return
     * @throws Exception
     */
    @Path("getByAppId")
    public Response findByAppId(@QueryParam(APP_ID) long appId) throws Exception {
        return Responses.success(service.findByAppId(appId));
    }

    /**
     * 查询按应用id
     * @param id
     * @param model
     * @return
     * @throws Exception
     */
    @Override
    @Path("update")
    public Response update(@QueryParam(ID) String id, ApplicationToken model) throws Exception {
        model.initializeTime();
        model.setApplication(application.identity());
        return Responses.success(service.update(model));
    }
    @Override
    @Path("delete")
    public Response delete(@QueryParam(ID) String id) throws Exception {
        ApplicationToken newModel = service.findById(Long.valueOf(id));
        int count = service.delete(newModel);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        //publish(); 暂不进行发布消息
        return Responses.success();
    }


}
