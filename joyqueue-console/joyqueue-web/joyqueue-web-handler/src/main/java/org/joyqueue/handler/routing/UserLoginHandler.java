/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.handler.routing;


import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.handler.RemoteIpHandler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.impl.HttpStatusException;
import org.joyqueue.model.domain.User;
import org.joyqueue.service.UserService;

import javax.validation.constraints.NotNull;

import static com.jd.laf.web.vertx.Environment.REMOTE_IP;
import static com.jd.laf.web.vertx.Environment.USER_KEY;
import static com.jd.laf.web.vertx.response.Response.HTTP_FORBIDDEN;
import static com.jd.laf.web.vertx.response.Response.HTTP_INTERNAL_ERROR;

/**
 * @author jiangnan53
 * @date 2020/6/10
 **/
public class UserLoginHandler extends RemoteIpHandler {

    @Value(value = "user.session.key", defaultValue = "user")
    protected String userSessionKey;

    @Value
    @NotNull
    protected UserService userService;

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest request = context.request();
        context.put(REMOTE_IP, getRemoteIP(request));
        Session session = context.session();
        if (session == null) {
            context.fail(new HttpStatusException(HTTP_INTERNAL_ERROR, "No session - did you forget to include a SessionHandler?"));
            return;
        }
        try {
            JsonObject requestBody = context.getBodyAsJson();
            if (context.request().absoluteURI().endsWith("/user/login")
                    && requestBody != null
                    && requestBody.containsKey("username")
                    && requestBody.containsKey("password")) {
                String username = requestBody.getString("username");
                String password = requestBody.getString("password");
                User user = userService.findUserByNameAndPassword(username, password);
                if (user != null) {
                    session.put(userSessionKey, user);
                    context.put(USER_KEY, user);
                    context.next();
                    return;
                } else {
                    session.remove(userSessionKey);
                    context.remove(USER_KEY);
                    context.clearUser();
                    context.fail(new HttpStatusException(HTTP_FORBIDDEN, "Forbidden - Username or Password is wrong"));
                    return;
                }
            }
        } catch (Exception ignore) {
        }

        User user = session.get(userSessionKey);
        if (user != null) {
            context.put(USER_KEY, user);
            context.next();
        } else {
            context.fail(new HttpStatusException(HTTP_FORBIDDEN, "Forbidden - User session is expire"));
        }
    }


    @Override
    public String type() {
        return "UserLogin";
    }
}
