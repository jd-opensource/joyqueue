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
package org.joyqueue.broker.manage.exporter.vertx;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.joyqueue.monitor.RestResponse;
import org.joyqueue.monitor.StringResponse;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * RestHandler
 *
 * author: gaohaoxiang
 * date: 2018/10/16
 */
public class RestHandler implements Handler<RoutingContext> {

    protected static final Logger logger = LoggerFactory.getLogger(RestHandler.class);

    private HandlerInvoker handlerInvoker;

    public RestHandler(HandlerInvoker handlerInvoker) {
        this.handlerInvoker = handlerInvoker;
    }

    @Override
    public void handle(RoutingContext context) {
        RestResponse response = null;
        try {
            Object result = handlerInvoker.invoke(context);
            if (result instanceof RestResponse) {
                response = (RestResponse) result;
            } else {
                response = RestResponse.success(result);
            }
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                t = ((InvocationTargetException) t).getTargetException();
            }
            if (t instanceof IllegalArgumentException) {
                logger.debug("request IllegalArgumentException, path: {}, params: {}", context.request().path(), context.request().params(), t);
                response = RestResponse.paramError(t.getMessage());
            } else {
                logger.error("request exception, path: {}, params: {}", context.request().path(), context.request().params(), t);
                response = RestResponse.serverError(t.toString());
            }
        }

        HttpServerResponse httpResponse = context.response();
        if (response.getData() instanceof StringResponse) {
            for (Map.Entry<String, String> entry : ((StringResponse) response.getData()).getHeaders().entrySet()) {
                httpResponse.putHeader(entry.getKey(), entry.getValue());
            }
            httpResponse.end(((StringResponse) response.getData()).getBody());
        } else {
            httpResponse.putHeader("Content-Type", "application/json;charset=utf-8");
            httpResponse.end(JSON.toJSONString(response, SerializerFeature.PrettyFormat, SerializerFeature.DisableCircularReferenceDetect));
        }
    }
}