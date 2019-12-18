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
package io.chubao.joyqueue.handler.routing.command;

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
import io.chubao.joyqueue.exception.ValidationException;
import io.chubao.joyqueue.handler.annotation.GenericValue;
import io.chubao.joyqueue.handler.annotation.Operator;
import io.chubao.joyqueue.handler.error.ConfigException;
import io.chubao.joyqueue.handler.error.ErrorCode;
import io.chubao.joyqueue.handler.message.AuditLogMessage;
import io.chubao.joyqueue.model.Query;
import io.chubao.joyqueue.model.domain.Identity;
import io.chubao.joyqueue.model.domain.OperLog;
import io.chubao.joyqueue.model.domain.User;
import io.chubao.joyqueue.nsr.NsrService;
import io.vertx.core.Vertx;

import static io.chubao.joyqueue.handler.Constants.ID;
import static io.chubao.joyqueue.handler.Constants.USER_KEY;
import static io.chubao.joyqueue.handler.message.MessageType.AUDIT_LOG;

/**
 * Created by wangxiaofei1 on 2019/1/4.
 */
public abstract class NsrCommandSupport<M, S extends NsrService, Q extends Query> implements Command<Response>, Poolable {
    @GenericValue
    protected S service;
    @Value(USER_KEY)
    protected User session;
    @Operator
    protected Identity operator;
    @CVertx
    protected Vertx vertx;

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
    public Response delete(@QueryParam(ID) String id) throws Exception {
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
    public Response get(@QueryParam(ID) String id) throws Exception {
        M model = (M) service.findById(id);
        if (model == null) {
            throw new ConfigException(getErrorCode());
        }
        return Responses.success(model);
    }

    @Path("update")
    public Response update(@QueryParam(ID) String id, @Body M model) throws Exception {
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
    public Response updateStatus(@QueryParam(ID) String id, @Body M model) throws Exception {
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
            vertx.eventBus().send(AUDIT_LOG.value(), new AuditLogMessage(operator.getCode(), type + "("
                    + JSON.toJSONString(model) + ")", auditType, type + "(" + model.toString() + ")"));
        }

    }
}
