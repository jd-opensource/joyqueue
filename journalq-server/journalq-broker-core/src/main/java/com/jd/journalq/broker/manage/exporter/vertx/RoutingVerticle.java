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
package com.jd.journalq.broker.manage.exporter.vertx;

import com.google.common.collect.Maps;
import com.jd.journalq.toolkit.util.ASMUtils;
import com.jd.laf.web.vertx.Environment;
import com.jd.laf.web.vertx.RouteProvider;
import com.jd.laf.web.vertx.config.RouteConfig;
import com.jd.laf.web.vertx.config.VertxConfig;
import com.jd.laf.web.vertx.message.RouteMessage;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

import static com.jd.laf.web.vertx.config.VertxConfig.Builder.build;
import static com.jd.laf.web.vertx.config.VertxConfig.Builder.inherit;

/**
 * RoutingVerticle
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/16
 */
public class RoutingVerticle extends com.jd.laf.web.vertx.RoutingVerticle {

    private static final String SERVICE_SEPARATOR = ".";

    private static final String BASE_ROUTING = "manage/base_routing.xml";

    private static final String ROUTING = "manage/routing.xml";

    private final Map<String /** key **/, Object> serviceMap = Maps.newHashMap();

    public RoutingVerticle(Environment env, HttpServerOptions httpOptions) {
        super(env, httpOptions, ROUTING);
        Vertx vertx = Vertx.vertx();
        init(vertx, vertx.getOrCreateContext());
    }

    public void registerServices(Map<String, Object> serviceMap) {
        this.serviceMap.putAll(serviceMap);
    }

    public void registerService(String key, Object service) {
        serviceMap.put(key, service);
    }


    @Override
    protected void buildHandlers(Route route, RouteConfig config, Environment environment) {
        String handler = config.getHandlers().get(0);
        String[] splitsHandler = StringUtils.splitByWholeSeparator(handler, SERVICE_SEPARATOR);

        if (splitsHandler.length != 2) {
            throw new IllegalArgumentException("handler error");
        }

        String serviceKey = splitsHandler[0];
        String methodName = splitsHandler[1];
        Object service = serviceMap.get(serviceKey);

        if (service == null) {
            throw new IllegalArgumentException(String.format("service %s not exist", serviceKey));
        }

        Map<String, Class<?>> params = ASMUtils.getParams(service.getClass(), methodName);
        ConvertInvoker handlerInvoker = new ConvertInvoker(service, methodName, params);
        route.handler(new RestHandler(handlerInvoker));
    }

}