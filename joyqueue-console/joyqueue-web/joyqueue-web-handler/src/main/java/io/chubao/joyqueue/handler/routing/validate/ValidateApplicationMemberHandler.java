/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.handler.routing.validate;

import io.chubao.joyqueue.handler.error.ConfigException;
import io.chubao.joyqueue.handler.error.ErrorCode;
import io.chubao.joyqueue.model.domain.Application;
import io.chubao.joyqueue.model.domain.User;
import io.chubao.joyqueue.model.domain.User.UserRole;
import io.chubao.joyqueue.service.UserService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.parameter.Parameters;
import io.vertx.ext.web.RoutingContext;

import static io.chubao.joyqueue.handler.Constants.APPLICATION;
import static io.chubao.joyqueue.handler.Constants.USER_KEY;

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
