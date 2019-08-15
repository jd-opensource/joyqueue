package io.chubao.joyqueue.handler.routing.validate;

import io.chubao.joyqueue.handler.error.ConfigException;
import io.chubao.joyqueue.handler.error.ErrorCode;
import io.chubao.joyqueue.model.domain.User;
import io.chubao.joyqueue.model.domain.User.UserRole;
import com.jd.laf.web.vertx.RoutingHandler;
import io.vertx.ext.web.RoutingContext;

import static io.chubao.joyqueue.handler.Constants.ADMIN;
import static io.chubao.joyqueue.handler.Constants.USER_KEY;

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
        User user = context.get(USER_KEY);
        if (user == null || user.getRole() != UserRole.ADMIN.value()) {
            throw new ConfigException(ErrorCode.NoPrivilege);
        }
        context.put(ADMIN, Boolean.TRUE);
        context.next();
    }
}
