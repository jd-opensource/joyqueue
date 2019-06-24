/**
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
package com.jd.joyqueue.handler.routing.validate;

import com.jd.joyqueue.handler.error.ConfigException;
import com.jd.joyqueue.handler.error.ErrorCode;
import com.jd.joyqueue.model.domain.Application;
import com.jd.joyqueue.model.domain.User;
import com.jd.joyqueue.model.domain.User.UserRole;
import com.jd.joyqueue.service.UserService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.parameter.Parameters;
import io.vertx.ext.web.RoutingContext;

import static com.jd.joyqueue.handler.Constants.APPLICATION;
import static com.jd.joyqueue.handler.Constants.USER_KEY;

/**
 * 判断当前用户是否是应用负责人
 * Created by yangyang115 on 18-8-3.
 */
public class ValidateApplicationOwnerHandler extends ValidateHandler {

    @Value
    protected UserService userService;

    @Override
    public String type() {
        return "validateApplicationOwner";
    }

    @Override
    protected void validate(final RoutingContext context, final Parameters.RequestParameter parameter) {
        Application application = context.get(APPLICATION);
        User user = context.get(USER_KEY);
        if (application != null && (application.isOwner(user) || user.getRole() == UserRole.ADMIN.value())) {
            return;
        }
        throw new ConfigException(ErrorCode.NoPrivilege);
    }
}
