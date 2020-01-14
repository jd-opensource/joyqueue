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
package org.joyqueue.handler.routing;

import org.joyqueue.model.domain.User;
import org.joyqueue.service.UserService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.handler.RemoteIpHandler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.impl.HttpStatusException;

import javax.validation.constraints.NotNull;

import static com.jd.laf.web.vertx.Environment.REMOTE_IP;
import static com.jd.laf.web.vertx.Environment.USER_KEY;
import static com.jd.laf.web.vertx.response.Response.HTTP_INTERNAL_ERROR;

/**
 * Created by chenyanying3 on 2019/4/13.
 */
public class AdminLoginHandler extends RemoteIpHandler {
    @Value(value = "user.session.key", defaultValue = "user")
    protected String userSessionKey;
    @Value
    @NotNull
    protected UserService userService;

    protected static final String DEFAULT_LOGIN_USER_CODE = "admin";

    @Override
    public String type() {
        return "AdminLogin";
    }

    @Override
    public void handle(final RoutingContext context) {
        HttpServerRequest request = context.request();
        Session session = context.session();
        if (session == null) {
            context.fail(new HttpStatusException(HTTP_INTERNAL_ERROR, "No session - did you forget to include a SessionHandler?"));
            return;
        }
        String remoteIP = getRemoteIP(request);
        context.put(REMOTE_IP, remoteIP);
        User user = session.get(userSessionKey);
        if (user == null) {
            user = userService.findByCode(DEFAULT_LOGIN_USER_CODE);
        }
        //存放用户上下文信息
        context.put(USER_KEY, user);
        context.next();
    }

}
