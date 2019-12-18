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
package io.chubao.joyqueue.handler.routing.validate;

import io.chubao.joyqueue.exception.ServiceException;
import io.chubao.joyqueue.handler.error.ConfigException;
import io.chubao.joyqueue.handler.error.ErrorCode;
import io.chubao.joyqueue.model.domain.Application;
import io.chubao.joyqueue.model.domain.ApplicationToken;
import io.chubao.joyqueue.service.ApplicationTokenService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.parameter.Parameters.RequestParameter;
import io.vertx.ext.web.RoutingContext;

import static io.chubao.joyqueue.handler.Constants.APPLICATION;
import static io.chubao.joyqueue.handler.Constants.ID;
import static io.chubao.joyqueue.handler.Constants.APP_TOKEN;

/**
 * 验证是指定应用的令牌
 */
public class ValidateTokenOfApplicationHandler extends ValidateHandler {

    @Value
    protected ApplicationTokenService applicationTokenService;

    @Override
    protected void validate(final RoutingContext context, final RequestParameter parameter) {
        Application app = context.get(APPLICATION);
        Long appTokenId = parameter.query().getLong(ID);
        ApplicationToken token = null;
        try {
            token = appTokenId == null ? null : applicationTokenService.findById(appTokenId);
        } catch (Exception e) {
            throw new ServiceException(ServiceException.NAMESERVER_RPC_ERROR,e.getMessage());
        }
        if (app == null) {
            throw new ConfigException(ErrorCode.ApplicationNotExists);
        } else if (token == null) {
            throw new ConfigException(ErrorCode.AppTokenNotExists);
        } else if (!token.getApplication().getCode().equals(app.getCode()) ) {
            throw new ConfigException(ErrorCode.NoPrivilege);
        }
        context.put(APP_TOKEN, token);

    }

    @Override
    public String type() {
        return "validateTokenOfApplication";
    }
}
