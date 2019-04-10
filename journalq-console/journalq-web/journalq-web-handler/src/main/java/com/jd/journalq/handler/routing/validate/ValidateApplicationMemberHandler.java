package com.jd.journalq.handler.routing.validate;

import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.journalq.model.domain.Application;
import com.jd.journalq.model.domain.User;
import com.jd.journalq.model.domain.User.UserRole;
import com.jd.journalq.service.UserService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.parameter.Parameters;
import io.vertx.ext.web.RoutingContext;

import static com.jd.journalq.handler.Constants.APPLICATION;
import static com.jd.journalq.handler.Constants.USER_KEY;

/**
 * 判断当前用户是否是应用成员
 * Created by yangyang115 on 18-8-3.
 */
public class ValidateApplicationMemberHandler extends ValidateHandler {

    @Value
    protected UserService userService;

    @Override
    public String type() {
        return "validateApplicationMember";
    }

    @Override
    protected void validate(final RoutingContext context, final Parameters.RequestParameter parameter) {
        Application application = context.get(APPLICATION);
        User user = context.get(USER_KEY);
        if (application == null || user == null) {
            throw new ConfigException(ErrorCode.NoPrivilege);
        } else if (application.isOwner(user)) {
            return;
        } else if (user.getRole() == UserRole.ADMIN.value()) {
            //管理员
            return;
        } else if (!userService.belong(user.getId(), application.getId())) {
            throw new ConfigException(ErrorCode.NoPrivilege);
        }
    }
}
