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
package org.joyqueue.handler.render;

import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.render.Render;
import com.jd.laf.web.vertx.response.Response;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

/**
 * Properties render
 * Created by chenyanying3 on 18-11-16.
 */
public class PropertiesRender implements Render {

    public static final String APPLICATION_PROPERTIES = "application/properties";

    @Override
    public void render(final RoutingContext context) {
        //获取结果
        Response result = context.get(Command.RESULT);
        HttpServerResponse response = context.response();
        response.putHeader(CONTENT_TYPE, APPLICATION_PROPERTIES);
        if (result != null && result.getCode() != Response.HTTP_OK) {
            //异常
            response.setStatusCode(result.getStatus()).end(result.getMessage());
        } else {
            response.end();
        }
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public String type() {
        return APPLICATION_PROPERTIES;
    }
}
