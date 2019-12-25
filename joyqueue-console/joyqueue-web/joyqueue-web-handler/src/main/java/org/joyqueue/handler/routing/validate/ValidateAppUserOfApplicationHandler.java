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
package org.joyqueue.handler.routing.validate;

import org.joyqueue.handler.error.ConfigException;
import org.joyqueue.handler.error.ErrorCode;
import org.joyqueue.handler.Constants;
import org.joyqueue.model.domain.Application;
import org.joyqueue.model.domain.ApplicationUser;
import org.joyqueue.service.UserService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.parameter.Parameters.RequestParameter;
import io.vertx.ext.web.RoutingContext;

import javax.validation.constraints.NotNull;

/**
 * 验证是指定应用的成员
 */
public class ValidateAppUserOfApplicationHandler extends ValidateHandler {
    @Value
    @NotNull
    protected UserService userService;

    @Override
    protected void validate(final RoutingContext context, final RequestParameter parameter) {
        Application application = context.get(Constants.APPLICATION);
        //appUserId参数
        Long appUserId = parameter.query().getLong(Constants.APP_USER_ID);
        //userId参数
        Long userId = parameter.query().getLong(Constants.USER_ID);
        ApplicationUser user = appUserId != null ? userService.findAppUserById(appUserId) :
                (userId != null && application != null ? userService.findAppUserByAppIdAndUserId(application.getId(), userId) : null);
        if (application == null) {
            throw new ConfigException(ErrorCode.ApplicationNotExists);
        } else if (user == null) {
            throw new ConfigException(ErrorCode.AppUserNotExists);
        } else if (user.getApplication().getId() != application.getId()) {
            throw new ConfigException(ErrorCode.NoPrivilege);
        }
        context.put(Constants.APP_USER, user);

    }

    @Override
    public String type() {
        return "validateAppUserOfApplication";
    }
}
