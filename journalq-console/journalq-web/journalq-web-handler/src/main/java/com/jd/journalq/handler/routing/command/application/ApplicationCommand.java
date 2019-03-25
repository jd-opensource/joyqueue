package com.jd.journalq.handler.routing.command.application;

import com.jd.journalq.model.ListQuery;
import com.jd.journalq.model.PageResult;
import com.jd.journalq.handler.binder.annotation.Page;
import com.jd.journalq.handler.binder.annotation.Path;
import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.journalq.handler.routing.command.CommandSupport;
import com.jd.journalq.model.domain.Application;
import com.jd.journalq.model.domain.TopicUnsubscribedApplication;
import com.jd.journalq.model.domain.User;
import com.jd.journalq.model.query.QApplication;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.service.ApplicationService;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

/**
 * Created by wangxiaofei1 on 2018/10/19.
 */
public class ApplicationCommand extends CommandSupport<Application,ApplicationService,QApplication> {

    @Override
    @Path("search")
    public Response pageQuery(@Page(typeindex = 2)QPageQuery<QApplication> qPageQuery) throws Exception {
        boolean admin = (session.getRole()== User.UserRole.ADMIN.value());
        if (qPageQuery.getQuery()== null) {
            qPageQuery.setQuery(admin ? new QApplication():new QApplication(operator.getId()));
        } else {
            qPageQuery.getQuery().setUserId(admin ? null : operator.getId());
        }
        return super.pageQuery(qPageQuery);
    }

    @Path("searchSubscribed")
    public Response searchSubscribed(@Page(typeindex = 2) QPageQuery<QApplication> qPageQuery) throws Exception {
        QApplication qApplication = qPageQuery.getQuery();
        if (qApplication == null) {
            throw new ConfigException(ErrorCode.BadRequest);
        }
        qApplication.setUserId(session.getId());
        qApplication.setAdmin(session.getRole()==User.UserRole.ADMIN.value() ? Boolean.TRUE : Boolean.FALSE);
        qApplication.setKeyword(qApplication.getKeyword()==null?null:qApplication.getKeyword().trim());
        PageResult<Application> result = service.findSubscribedByQuery(qPageQuery);
        return Responses.success(result.getPagination(), result.getResult());
    }

    @Path("searchUnsubscribed")
    public Response searchUnsubscribed(@Page(typeindex = 2) QPageQuery<QApplication> qPageQuery) throws Exception {
        QApplication qApplication = qPageQuery.getQuery();
        if (qApplication == null) {
            throw new ConfigException(ErrorCode.BadRequest);
        }
        qApplication.setUserId(session.getId());
        qApplication.setAdmin(session.getRole()==User.UserRole.ADMIN.value() ? Boolean.TRUE : Boolean.FALSE);
        qApplication.setKeyword(qApplication.getKeyword()==null?null:qApplication.getKeyword().trim());
        PageResult<TopicUnsubscribedApplication> result = service.findTopicUnsubscribedByQuery(qPageQuery);
        return Responses.success(result.getPagination(), result.getResult());
    }

    @Path("findAll")
    public Response findAll() throws Exception {
       return Responses.success(service.findByQuery(new ListQuery<>(new QApplication())));
    }
}
