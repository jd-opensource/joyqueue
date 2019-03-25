package com.jd.journalq.handler.routing.validate;

import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.journalq.handler.Constants;
import com.jd.journalq.model.domain.User;
import com.jd.journalq.model.domain.User.UserRole;
import com.jd.laf.web.vertx.RoutingHandler;
import io.vertx.ext.web.RoutingContext;

/**
 * 验证系统管理所需要的管理员角色
 * Created by yangyang115 on 18-8-3.
 */
public class ValidateAdminHandler implements RoutingHandler {

    @Override
    public String type() {
        return "validateAdmin";
    }

    @Override
    public void handle(final RoutingContext context) {
        User user = context.get(Constants.USER_KEY);
        if (user == null || user.getRole() != UserRole.ADMIN.value()) {
            throw new ConfigException(ErrorCode.NoPrivilege);
        }
        context.put(Constants.ADMIN, Boolean.TRUE);
        context.next();
    }
}
