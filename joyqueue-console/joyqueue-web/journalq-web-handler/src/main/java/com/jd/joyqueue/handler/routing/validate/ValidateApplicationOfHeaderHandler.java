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
import com.jd.joyqueue.service.ApplicationService;
import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.parameter.Parameter;
import com.jd.laf.web.vertx.parameter.Parameters;
import io.vertx.ext.web.RoutingContext;

import static com.jd.joyqueue.handler.Constants.APPLICATION;

/**
 * Created by yangyang36 on 2018/9/17.
 */
public class ValidateApplicationOfHeaderHandler extends ValidateHandler {

    @Value
    protected ApplicationService applicationService;

    @Override
    protected void validate(RoutingContext context, Parameters.RequestParameter parameter) {
        Parameter header = parameter.header();
        String appCode = header.getString(APPLICATION);
        if (appCode == null || appCode.isEmpty()) {
            throw new ConfigException(ErrorCode.BadRequest, "请求头没有应用代码");
        }
        Application application = applicationService.findByCode(appCode);
        if (application == null) {
            throw new ConfigException(ErrorCode.ApplicationNotExists);
        }
        context.put(APPLICATION, application);
    }

    @Override
    public String type() {
        return "validateAppOfHeader";
    }
}
