package com.jd.journalq.handler.routing.command.user;

import com.jd.journalq.model.domain.Identity;
import com.jd.journalq.model.domain.User;
import com.jd.journalq.sync.SyncService;
import com.jd.journalq.sync.UserInfo;
import com.jd.journalq.handler.Constants;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

import javax.validation.constraints.NotNull;

import static com.jd.journalq.handler.Constants.USER_KEY;
import static com.jd.laf.web.vertx.annotation.Body.BodyType.JSON;

/**
 * 同步用户
 * Created by yangyang115 on 18-7-26.
 */
public class SyncUserCommand implements Command<Response>, Poolable {

    //应用服务
    @Value(nullable = false)
    protected SyncService syncService;

    @Body(type = JSON)
    @NotNull
    protected User user;
    @Value(USER_KEY)
    protected User session;

    @Override
    public String type() {
        return "syncUser";
    }

    @Override
    public Response execute() throws Exception {
//        UserInfo info = syncService.syncUser(user);
//        if (info == null) {
//            throw new ConfigException(ErrorCode.UserNotExists);
//        }
        UserInfo info = new UserInfo();
        info.setCode(user.getCode());
        info.setUser(new Identity(session));
        return Responses.success(syncService.addOrUpdateUser(info));
    }

    //  @Override
    public void clean() {
        user = null;
        session = null;
    }
}
