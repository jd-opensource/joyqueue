package com.jd.journalq.handler.routing.command.user;

import com.jd.journalq.handler.annotation.PageQuery;
import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.journalq.handler.routing.command.CommandSupport;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.model.domain.Identity;
import com.jd.journalq.model.domain.User;
import com.jd.journalq.model.query.QUser;
import com.jd.journalq.service.UserService;
import com.jd.journalq.service.UserTokenService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;


import java.util.List;

import static com.jd.journalq.handler.Constants.CODE;
import static com.jd.journalq.handler.Constants.ID;
import static com.jd.journalq.handler.Constants.USER_KEY;

/**
 * @author wylixiaobin
 * Date: 2018/10/17
 */
public class UserCommand extends CommandSupport<User, UserService, QUser> {
    @Value(USER_KEY)
    protected User operator;

    @Value(nullable = false)
    private UserTokenService userTokenService;
    @Override
    @Path("add")
    public Response add(@Body User model) throws Exception {
        model.setCreateBy(new Identity(-1L));
        model.setUpdateBy(new Identity(-1L));
        super.add(model);
        return Responses.success(model);
    }

    @Override
    @Path("search")
    public Response pageQuery(@PageQuery QPageQuery<QUser> qPageQuery) throws Exception {
        if (qPageQuery.getQuery()== null) {
            qPageQuery.setQuery(new QUser());
        }
        return super.pageQuery(qPageQuery);
    }

    @Override
    @Path("update")
    public Response update(@QueryParam(ID) Long id, @Body User model) throws Exception {
        if (operator == null || operator.getRole() != User.UserRole.ADMIN.value()) {
            throw new ConfigException(ErrorCode.NoPrivilege);
        }
        return super.update(id, model);
    }

    @Path("getByCode")
    public Response getByCode(@QueryParam(CODE) String code) {
        return Responses.success(service.findByCode(code));
    }


}
