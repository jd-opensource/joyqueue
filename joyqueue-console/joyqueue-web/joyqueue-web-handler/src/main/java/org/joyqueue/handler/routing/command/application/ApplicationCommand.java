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
package org.joyqueue.handler.routing.command.application;

import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.joyqueue.handler.annotation.PageQuery;
import org.joyqueue.handler.error.ConfigException;
import org.joyqueue.handler.error.ErrorCode;
import org.joyqueue.handler.routing.command.CommandSupport;
import org.joyqueue.handler.Constants;
import org.joyqueue.model.ListQuery;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.domain.Application;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.TopicUnsubscribedApplication;
import org.joyqueue.model.domain.User;
import org.joyqueue.model.query.QApplication;
import org.joyqueue.service.ApplicationService;
import org.joyqueue.service.UserService;

import javax.validation.constraints.NotNull;

/**
 * Created by wangxiaofei1 on 2018/10/19.
 */
public class ApplicationCommand extends CommandSupport<Application,ApplicationService,QApplication> {

    @Value
    @NotNull
    protected UserService userService;

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

    @Path("setOwner")
    public Response setOwner(@QueryParam(Constants.ID) Long id, @QueryParam(Constants.USER_ID) Long userId) throws Exception {
        Application application = service.findById(id);
        User user = userService.findById(userId);
        application.setOwner(new Identity(user.getId(), user.getCode()));
        return Responses.success(service.update(application));
    }

    @Path("getByCode")
    public Response getByCode(@QueryParam(Constants.APP_CODE) String appCode) throws Exception {
        return Responses.success(service.findByCode(appCode));
    }

}
