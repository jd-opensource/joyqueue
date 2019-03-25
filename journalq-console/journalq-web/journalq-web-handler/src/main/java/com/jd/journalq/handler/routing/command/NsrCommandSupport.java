package com.jd.journalq.handler.routing.command;

import com.jd.journalq.exception.ValidationException;
import com.jd.journalq.handler.binder.annotation.*;
import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.journalq.handler.message.AuditLogMessage;
import com.jd.journalq.handler.Constants;
import com.jd.journalq.handler.message.MessageType;
import com.jd.journalq.model.*;
import com.jd.journalq.model.domain.Identity;
import com.jd.journalq.model.domain.OperLog;
import com.jd.journalq.model.domain.User;
import com.jd.journalq.nsr.NsrService;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.annotation.CVertx;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import io.vertx.core.Vertx;
import org.apache.commons.lang3.StringUtils;

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

    private String module;

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

    @Path("search")
    public Response pageQuery(@Page(typeindex = 2) QPageQuery<Q> qPageQuery) throws Exception {
        Preconditions.checkArgument(qPageQuery!=null, "Illegal args.");
        if(qPageQuery.getQuery() != null) {
            if (qPageQuery.getQuery() instanceof QOperator) {
                QOperator query = (QOperator) qPageQuery.getQuery();
                query.setUserId(session.getId());
                query.setRole(session.getRole());
                query.setUserCode(session.getCode());
                query.setUserName(session.getName());
                query.setAdmin(session.getRole()==User.UserRole.ADMIN.value() ? Boolean.TRUE : Boolean.FALSE);
            }
            if (qPageQuery.getQuery() instanceof QKeyword) {
                String keyword = ((QKeyword) qPageQuery.getQuery()).getKeyword();
                ((QKeyword) qPageQuery.getQuery()).setKeyword(StringUtils.isBlank(keyword)? null:keyword.trim());
            }
        }

        PageResult<M> result  = service.findByQuery(qPageQuery);

        return Responses.success(result.getPagination(), result.getResult());
    }

    @Path("add")
    public Response add(@GenericBody(type = GenericBody.BodyType.JSON,typeindex = 0) M model) throws Exception {
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
    public Response delete(@ParamterValue(Constants.ID) Object id) throws Exception {
        M newModel = (M) service.findById(id.toString());
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
    public Response get(@ParamterValue(Constants.ID) Object id) throws Exception {
        M model = (M) service.findById(id.toString());
        if (model == null) {
            throw new ConfigException(getErrorCode());
        }
        return Responses.success(model);
    }

    @Path("update")
    public Response update(@ParamterValue(Constants.ID)Object id, @GenericBody(type = GenericBody.BodyType.JSON,typeindex = 0) M model) throws Exception {
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
    public Response updateStatus(@ParamterValue(Constants.ID)Object id, @GenericBody(type = GenericBody.BodyType.JSON,typeindex = 0) M model) throws Exception {
        int count = service.update(model);
        if (count < 1) {
            throw new ConfigException(updateErrorCode());
        }
        return Responses.success(model);
    }
    protected String getModule() {
        //module默认值与type相同
        module = this.type();
        return module;
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
            vertx.eventBus().send(MessageType.AUDIT_LOG.value(), new AuditLogMessage(operator.getCode(), this.module + "("
                    + com.alibaba.fastjson.JSON.toJSONString(model) + ")", auditType, this.module + "(" + model.toString() + ")"));
        }

        //todo url待完善
//        if (operType != null) {
//            OperLog.Type type = OperLog.Type.resolve(module);
//            vertx.eventBus().send(OPER_LOG.value(), new OperLogMessage(operType.value(),
//                    type==null? -1:type.value(), String.valueOf(model), com.alibaba.fastjson.JSON.toJSONString(model), operator.getId()));
//        }
    }
}
