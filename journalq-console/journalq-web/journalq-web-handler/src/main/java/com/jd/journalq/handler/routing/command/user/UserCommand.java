package com.jd.journalq.handler.routing.command.user;

import com.jd.journalq.handler.binder.annotation.*;
import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.journalq.handler.routing.command.CommandSupport;

import com.jd.journalq.handler.Constants;
import com.jd.journalq.model.domain.Identity;
import com.jd.journalq.model.domain.User;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.model.query.QUser;
import com.jd.journalq.service.UserService;

import com.jd.journalq.service.UserTokenService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;


import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2018/10/17
 */
public class UserCommand extends CommandSupport<User, UserService, QUser> {
    @Value(Constants.USER_KEY)
    protected User operator;

    @GenericValue
    private UserTokenService userTokenService;
    @Override
    @Path("add")
    public Response add(@GenericBody(type = GenericBody.BodyType.JSON,typeindex = 0)User model) throws Exception {
        model.setCreateBy(new Identity(-1l));
        model.setUpdateBy(new Identity(-1l));
        super.add(model);
        return Responses.success(model);
    }

    @Override
    @Path("search")
    public Response pageQuery(@Page(typeindex = 2)QPageQuery<QUser> qPageQuery) throws Exception {
        if (qPageQuery.getQuery()== null) {
            qPageQuery.setQuery(new QUser());
        }
        return super.pageQuery(qPageQuery);
    }

    @Override
    @Path("update")
    public Response update(@ParamterValue(Constants.ID)Object id, @GenericBody(type = GenericBody.BodyType.JSON,typeindex = 0)User model) throws Exception {
        if (operator == null || operator.getRole() != User.UserRole.ADMIN.value()) {
            throw new ConfigException(ErrorCode.NoPrivilege);
        }
        return super.update(id,model);
    }

    @Path("getByCode")
    public Response getByCode(@ParamterValue("code") Object code) {
        return Responses.success(service.findByCode((String) code));
    }

    @Path("getByIds")
    public Response getByIds(@ParamterValue("ids") Object ids) {
        return Responses.success(service.findByIds((List<String>) ids));
    }


}
