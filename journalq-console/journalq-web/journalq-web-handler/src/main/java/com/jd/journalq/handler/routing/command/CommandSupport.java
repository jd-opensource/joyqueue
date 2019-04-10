package com.jd.journalq.handler.routing.command;

import com.alibaba.fastjson.JSON;
import com.jd.journalq.exception.ValidationException;
import com.jd.journalq.handler.annotation.GenericValue;
import com.jd.journalq.handler.annotation.Operator;
import com.jd.journalq.handler.annotation.PageQuery;
import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.journalq.handler.message.AuditLogMessage;
import com.jd.journalq.model.*;
import com.jd.journalq.model.domain.BaseModel;
import com.jd.journalq.model.domain.Identity;
import com.jd.journalq.model.domain.OperLog;
import com.jd.journalq.model.domain.User;
import com.jd.journalq.service.PageService;
import com.jd.journalq.toolkit.lang.Preconditions;
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
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

import static com.jd.journalq.handler.Constants.ID;
import static com.jd.journalq.handler.Constants.USER_KEY;
import static com.jd.journalq.handler.message.MessageType.AUDIT_LOG;


/** 公共接口
 * @author wylixiaobin
 * Date: 2018/10/16
 */
public abstract class CommandSupport <M extends BaseModel, S extends PageService, Q extends Query> implements Command<Response>, Poolable {

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

    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<Q> qPageQuery) throws Exception {
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
    public Response add(@Body M model) throws Exception {
        model.setCreateBy(new Identity(session));
        model.setCreateTime(new Date());
        model.setUpdateBy(model.getCreateBy());
        model.setUpdateTime(new Date());
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
    public Response delete(@QueryParam(ID) Long id) throws Exception {
        M newModel = (M) service.findById(id);
        newModel.setStatus(BaseModel.DELETED);
        newModel.setUpdateBy(operator);
        newModel.setUpdateTime(new Date());
        int count = service.delete(newModel);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        publish(AuditLogMessage.ActionType.DELETE, OperLog.OperType.DELETE, newModel);
        return Responses.success();
    }

    @Path("get")
    public Response get(@QueryParam(ID) Long id) throws Exception {
        M model = (M) service.findById(id);
        if (model == null) {
            throw new ConfigException(getErrorCode());
        }
        return Responses.success(model);
    }

    @Path("update")
    public Response update(@QueryParam(ID) Long id, @Body M model) throws Exception {
        model.setUpdateBy(operator);
        model.setUpdateTime(new Date());

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
    public Response updateStatus(@QueryParam(ID) Long id, @Body M model) throws Exception {
        model.setUpdateBy(operator);
        model.setUpdateTime(new Date());
        int count = service.updateStatus(model);
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
            String type = type();
            vertx.eventBus().send(AUDIT_LOG.value(), new AuditLogMessage(operator.getCode(), type + "("
                    + model.toString() + ")", auditType, type + "(" + JSON.toJSONString(model) + ")"));
        }

        //todo url待完善
//        if (operType != null) {
//            vertx.eventBus().send(OPER_LOG.value(), new OperLogMessage(operType.value(),
//                    OperLog.Type.resolve(module).value(), String.valueOf(model.getId()), com.alibaba.fastjson.JSON.toJSONString(model), operator.getId()));
//        }
    }

}
