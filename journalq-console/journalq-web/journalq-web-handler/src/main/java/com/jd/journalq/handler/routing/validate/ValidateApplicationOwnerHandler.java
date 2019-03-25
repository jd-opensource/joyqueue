package com.jd.journalq.handler.routing.validate;

import com.jd.journalq.service.UserService;
import com.jd.journalq.handler.Constants;
import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.laf.binding.annotation.Value;
import com.jd.journalq.model.domain.Application;
import com.jd.journalq.model.domain.User;
import com.jd.journalq.model.domain.User.UserRole;
import com.jd.laf.web.vertx.parameter.Parameters;
import io.vertx.ext.web.RoutingContext;

/**
 * 判断当前用户是否是应用负责人
 * Created by yangyang115 on 18-8-3.
 */
public class ValidateApplicationOwnerHandler extends ValidateHandler {

    @Value
    protected UserService userService;

    @Override
    public String type() {
        return "validateApplicationOwner";
    }

    @Override
    protected void validate(final RoutingContext context, final Parameters.RequestParameter parameter) {
        Application application = context.get(Constants.APPLICATION);
        User user = context.get(Constants.USER_KEY);
        if (application != null && (application.isOwner(user) || user.getRole() == UserRole.ADMIN.value())) {
            return;
        }
        throw new ConfigException(ErrorCode.NoPrivilege);
    }
}
