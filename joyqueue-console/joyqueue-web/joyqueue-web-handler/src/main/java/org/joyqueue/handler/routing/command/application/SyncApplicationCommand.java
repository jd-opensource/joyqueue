/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.handler.routing.command.application;


import com.jd.laf.web.vertx.annotation.Path;
import org.joyqueue.handler.error.ConfigException;
import org.joyqueue.handler.error.ErrorCode;
import org.joyqueue.handler.Constants;
import org.joyqueue.model.domain.Application;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.User;
import org.joyqueue.sync.ApplicationInfo;
import org.joyqueue.sync.SyncService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;

/**
 * 同步应用
 * Created by yangyang115 on 18-7-26.
 */
public class SyncApplicationCommand implements Command<Response>, Poolable {

    //应用服务
    @Value(nullable = false)
    protected SyncService syncService;

    @Value(Constants.USER_KEY)
    protected User session;

    @Path("syncApp")
    public Response add(@Body Application application) throws Exception {
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
        session = null;
    }

}
