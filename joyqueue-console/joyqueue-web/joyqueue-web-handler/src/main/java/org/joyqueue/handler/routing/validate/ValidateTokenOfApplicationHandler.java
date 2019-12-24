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

import org.joyqueue.exception.ServiceException;
import org.joyqueue.handler.error.ConfigException;
import org.joyqueue.handler.error.ErrorCode;
import org.joyqueue.handler.Constants;
import org.joyqueue.model.domain.Application;
import org.joyqueue.model.domain.ApplicationToken;
import org.joyqueue.service.ApplicationTokenService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.parameter.Parameters.RequestParameter;
import io.vertx.ext.web.RoutingContext;

/**
 * 验证是指定应用的令牌
 */
public class ValidateTokenOfApplicationHandler extends ValidateHandler {

    @Value
    protected ApplicationTokenService applicationTokenService;

    @Override
    protected void validate(final RoutingContext context, final RequestParameter parameter) {
        Application app = context.get(Constants.APPLICATION);
        Long appTokenId = parameter.query().getLong(Constants.ID);
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
        context.put(Constants.APP_TOKEN, token);

    }

    @Override
    public String type() {
        return "validateTokenOfApplication";
    }
}
