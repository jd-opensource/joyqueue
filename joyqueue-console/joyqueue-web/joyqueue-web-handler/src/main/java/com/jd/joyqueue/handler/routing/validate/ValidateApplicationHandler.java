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
import com.jd.joyqueue.model.domain.Identifier;
import com.jd.joyqueue.service.ApplicationService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.parameter.Parameter;
import com.jd.laf.web.vertx.parameter.Parameters;
import io.vertx.ext.web.RoutingContext;

import static com.jd.joyqueue.handler.Constants.APP_ID;
import static com.jd.joyqueue.handler.Constants.APP_CODE;
import static com.jd.joyqueue.handler.Constants.ID;
import static com.jd.joyqueue.handler.Constants.APPLICATION;

/**
 * 判断请求的应用是否和认证的应用身份一致，避免越权查看其它应用信息
 * Created by yangyang115 on 18-8-3.
 */
public class ValidateApplicationHandler extends ValidateHandler {

    @Value
    protected ApplicationService applicationService;

    @Override
    public String type() {
        return "validateApplication";
    }

    @Override
    protected void validate(final RoutingContext context, final Parameters.RequestParameter parameter) {
        Parameter query = parameter.query();
        String value = query.getString(APP_ID);
        value = value == null ? query.getString(APP_CODE) : value;
        value = value == null ? query.getString(ID) : value;
        Application application = context.get(APPLICATION);
        if (application != null) {
            //有认证的上下文
            if (application.getCode().equals(value)
                    || String.valueOf(application.getId()).equals(value)) {
                //和上下文一致
                return;
            }
            //越权访问
            throw new ConfigException(ErrorCode.NoPrivilege);
        }
        //没有认证上下文场景，通过ID或Code加载应用
        if (Character.isDigit(value.charAt(0))) {
            //可能是ID
            try {
                application = applicationService.findById(Long.parseLong(value));
            } catch (NumberFormatException e) {
            }
        }
        if (application == null) {
            if (Identifier.isIdentifier(value)) {
                //根据code查询
                application = applicationService.findByCode(value);
                if (application == null) {
                    throw new ConfigException(ErrorCode.ApplicationNotExists);
                }
            } else {
                throw new ConfigException(ErrorCode.ApplicationNotExists);
            }
        }
        context.put(APPLICATION, application);
    }
}
