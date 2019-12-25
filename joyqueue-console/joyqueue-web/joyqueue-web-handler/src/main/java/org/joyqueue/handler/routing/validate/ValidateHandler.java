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

import com.jd.laf.web.vertx.RoutingHandler;
import com.jd.laf.web.vertx.parameter.Parameters;
import com.jd.laf.web.vertx.parameter.Parameters.RequestParameter;
import io.vertx.ext.web.RoutingContext;

/**
 * 验证基础类
 */
public abstract class ValidateHandler implements RoutingHandler {

    @Override
    public void handle(final RoutingContext context) {
        validate(context, Parameters.get(context.request()));
        context.next();
    }

    /**
     * 验证
     *
     * @param context   上下文
     * @param parameter 请求参数
     */
    protected abstract void validate(RoutingContext context, RequestParameter parameter);
}