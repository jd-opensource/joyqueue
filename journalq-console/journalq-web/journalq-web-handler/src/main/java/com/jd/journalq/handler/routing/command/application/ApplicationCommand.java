/**
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
package com.jd.journalq.handler.routing.command.application;

import com.jd.journalq.handler.annotation.PageQuery;
import com.jd.journalq.model.ListQuery;
import com.jd.journalq.model.PageResult;
import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.journalq.handler.routing.command.CommandSupport;
import com.jd.journalq.model.domain.Application;
import com.jd.journalq.model.domain.TopicUnsubscribedApplication;
import com.jd.journalq.model.domain.User;
import com.jd.journalq.model.query.QApplication;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.service.ApplicationService;
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
