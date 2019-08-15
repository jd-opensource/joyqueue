package io.chubao.joyqueue.handler.routing.command.application;

import io.chubao.joyqueue.handler.annotation.PageQuery;
import io.chubao.joyqueue.model.ListQuery;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.handler.error.ConfigException;
import io.chubao.joyqueue.handler.error.ErrorCode;
import io.chubao.joyqueue.handler.routing.command.CommandSupport;
import io.chubao.joyqueue.model.domain.Application;
import io.chubao.joyqueue.model.domain.TopicUnsubscribedApplication;
import io.chubao.joyqueue.model.domain.User;
import io.chubao.joyqueue.model.query.QApplication;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.service.ApplicationService;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

/**
 * Created by wangxiaofei1 on 2018/10/19.
 */
public class ApplicationCommand extends CommandSupport<Application,ApplicationService,QApplication> {

    @Override
    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QApplication> qPageQuery) throws Exception {
        boolean admin = (session.getRole()== User.UserRole.ADMIN.value());
        if (qPageQuery.getQuery()== null) {
            qPageQuery.setQuery(admin ? new QApplication():new QApplication(operator.getId()));
        } else {
            qPageQuery.getQuery().setUserId(admin ? null : operator.getId());
        }
        return super.pageQuery(qPageQuery);
    }

    @Path("searchSubscribed")
    public Response searchSubscribed(@PageQuery QPageQuery<QApplication> qPageQuery) throws Exception {
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
    public Response searchUnsubscribed(@PageQuery QPageQuery<QApplication> qPageQuery) throws Exception {
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
