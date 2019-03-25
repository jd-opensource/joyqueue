package com.jd.journalq.handler.routing.command.application;

import com.jd.journalq.sync.ApplicationInfo;
import com.jd.journalq.sync.SyncService;
import com.jd.journalq.handler.Constants;
import com.jd.journalq.handler.error.ConfigException;
import com.jd.journalq.handler.error.ErrorCode;
import com.jd.laf.binding.annotation.Value;
import com.jd.journalq.model.domain.Application;
import com.jd.journalq.model.domain.Identity;
import com.jd.journalq.model.domain.User;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.CVertx;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import io.vertx.core.Vertx;

import javax.validation.constraints.NotNull;

import static com.jd.laf.web.vertx.annotation.Body.BodyType.JSON;

/**
 * 同步应用
 * Created by yangyang115 on 18-7-26.
 */
public class SyncApplicationCommand implements Command<Response>, Poolable {

    //应用服务
    @Value(nullable = false)
    protected SyncService syncService;

    @Body(type = JSON)
    @NotNull
    protected Application application;
    @Value(Constants.USER_KEY)
    protected User session;
    @CVertx
    private Vertx vertx;

    @Override
    public String type() {
        return "syncApp";
    }

    @Override
    public Response execute() throws Exception {
        application.setErp(session.getCode());
        ApplicationInfo info = syncService.syncApp(application);
        if (info == null) {
            throw new ConfigException(ErrorCode.ApplicationNotExists);
        }
        info.setUser(new Identity(session));
        return Responses.success(syncService.addOrUpdateApp(info));
    }

    @Override
    public void clean() {
        application = null;
        session = null;
    }

}
