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
package org.joyqueue.handler.routing.command;

import com.alibaba.fastjson.JSON;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.CVertx;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import io.vertx.core.Vertx;
import org.joyqueue.exception.ValidationException;
import org.joyqueue.handler.Constants;
import org.joyqueue.handler.annotation.GenericValue;
import org.joyqueue.handler.annotation.Operator;
import org.joyqueue.handler.error.ConfigException;
import org.joyqueue.handler.error.ErrorCode;
import org.joyqueue.handler.message.AuditLogMessage;
import org.joyqueue.handler.message.MessageType;
import org.joyqueue.model.Query;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.OperLog;
import org.joyqueue.model.domain.User;
import org.joyqueue.nsr.NsrService;
import org.joyqueue.service.ApplicationUserService;

/**
 * Created by wangxiaofei1 on 2019/1/4.
 */
public abstract class NsrCommandSupport<M, S extends NsrService, Q extends Query> implements Command<Response>, Poolable {
    @GenericValue
    protected S service;
    @Value(Constants.USER_KEY)
    protected User session;
    @Operator
    protected Identity operator;
    @CVertx
    protected Vertx vertx;
    @Value(nullable = false)
    protected ApplicationUserService applicationUserService;

    @Override
    public Response execute() throws Exception {
        return Responses.error(Response.HTTP_NOT_FOUND,Response.HTTP_NOT_FOUND,"Not Found");
    }

    @Override
    public void clean() {
        service = null;
        session = null;
        operator = null;
    }

//    @Path("search")
//    public Response pageQuery(@PageQuery QPageQuery<Q> qPageQuery) throws Exception {
//        Preconditions.checkArgument(qPageQuery!=null, "Illegal args.");
//        if(qPageQuery.getQuery() != null) {
//            if (qPageQuery.getQuery() instanceof QOperator) {
//                QOperator query = (QOperator) qPageQuery.getQuery();
//                query.setUserId(session.getId());
//                query.setRole(session.getRole());
//                query.setUserCode(session.getCode());
//                query.setUserName(session.getName());
//                query.setAdmin(session.getRole()==User.UserRole.ADMIN.value() ? Boolean.TRUE : Boolean.FALSE);
//            }
//            if (qPageQuery.getQuery() instanceof QKeyword) {
//                String keyword = ((QKeyword) qPageQuery.getQuery()).getKeyword();
//                ((QKeyword) qPageQuery.getQuery()).setKeyword(StringUtils.isBlank(keyword)? null:keyword.trim());
//            }
//        }
//
//        PageResult<M> result  = service.findByQuery(qPageQuery);
//
//        return Responses.success(result.getPagination(), result.getResult());
//    }

    @Path("add")
    public Response add(@Body M model) throws Exception {
        int count;
        try {
            count = service.add(model);
        } catch (ValidationException e) {
            return Responses.error(ErrorCode.ValidationError.getCode(), e.getStatus(), e.getMessage());
        }
        if (count <= 0) {
            throw new ConfigException(addErrorCode());
        }
        publish(AuditLogMessage.ActionType.ADD, OperLog.OperType.ADD, model);
        return Responses.success(model);
    }

    @Path("delete")
    public Response delete(@QueryParam(Constants.ID) String id) throws Exception {
        M newModel = (M) service.findById(id);
        if (newModel == null) {
            throw new ConfigException(deleteErrorCode());
        }
        int count = service.delete(newModel);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        publish(AuditLogMessage.ActionType.DELETE, OperLog.OperType.DELETE, newModel);
        return Responses.success();
    }

    @Path("get")
    public Response get(@QueryParam(Constants.ID) String id) throws Exception {
        M model = (M) service.findById(id);
        if (model == null) {
            throw new ConfigException(getErrorCode());
        }
        return Responses.success(model);
    }

    @Path("update")
    public Response update(@QueryParam(Constants.ID) String id, @Body M model) throws Exception {
        int count;
        try {
            count = service.update(model);
        } catch (ValidationException e) {
            return Responses.error(ErrorCode.ValidationError.getCode(), e.getStatus(), e.getMessage());
        }
        if (count < 1) {
            throw new ConfigException(updateErrorCode());
        }
        publish(AuditLogMessage.ActionType.UPDATE, OperLog.OperType.UPDATE, model);
        return Responses.success(model);
    }

    @Path("state")
    public Response updateStatus(@QueryParam(Constants.ID) String id, @Body M model) throws Exception {
        int count = service.update(model);
        if (count < 1) {
            throw new ConfigException(updateErrorCode());
        }
        return Responses.success(model);
    }

    public ErrorCode addErrorCode() {
        return ErrorCode.NoDataAdded;
    }

    public ErrorCode deleteErrorCode(){
        return ErrorCode.NoDataDeleted;
    }

    public ErrorCode updateErrorCode() {
        return ErrorCode.NoDataUpdated;
    }

    public ErrorCode getErrorCode(){
        return ErrorCode.NoDataExists;
    }

    public void publish(AuditLogMessage.ActionType auditType, OperLog.OperType operType, M model) {
        if (model == null) {
            return;
        }

        if (auditType != null) {
            String type = this.type();
            vertx.eventBus().send(MessageType.AUDIT_LOG.value(), new AuditLogMessage(operator.getCode(), type + "("
                    + JSON.toJSONString(model) + ")", auditType, type + "(" + model.toString() + ")"));
        }

    }

    // 权限约束：普通用户只有该应用下用户才能添加用户
    public void validatePrivilege(String appCode) {
        if (session.getRole() != User.UserRole.ADMIN.value() &&
                applicationUserService.findByUserApp(operator.getCode(), appCode) == null) {
            throw new ConfigException(ErrorCode.NoPrivilege);
        }
    }
}
