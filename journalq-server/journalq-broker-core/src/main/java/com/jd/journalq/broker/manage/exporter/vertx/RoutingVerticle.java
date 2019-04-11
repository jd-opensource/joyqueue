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
import com.jd.laf.web.vertx.config.RouteConfig;
import com.jd.laf.web.vertx.config.VertxConfig;
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
    }

    public void registerServices(Map<String, Object> serviceMap) {
        this.serviceMap.putAll(serviceMap);
    }

    public void registerService(String key, Object service) {
        serviceMap.put(key, service);
    }

    @Override
    public void start() throws Exception {
        try {
            init(Vertx.vertx(), null);

            //构建配置数据
            VertxConfig baseConfig = buildBaseConfig(BASE_ROUTING);
            config = inherit(buildConfig(baseConfig, file));

            Router router = createRouter(env);
            //构建业务处理链
            buildHandlers(router, config, env);
            //构建消息处理链
            buildConsumers(config);
            //启动服务
            httpServer = vertx.createHttpServer(httpOptions);
            httpServer.requestHandler(router::accept).listen(event -> {
                if (event.succeeded()) {
                    logger.info(String.format("success starting http server on port %d", httpServer.actualPort()));
                } else {
                    logger.error(String.format("failed starting http server on port %d",
                            httpServer.actualPort()), event.cause());
                }
            });
            logger.info(String.format("success starting routing verticle"));
        } catch (Exception e) {
            logger.error(String.format("failed starting routing verticle"), e);
            throw e;
        }
    }

    @Override
    public void stop() throws Exception {
        httpServer.close();
    }

    protected VertxConfig buildBaseConfig(String file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalStateException("file can not be empty.");
        }

        BufferedReader reader = null;
        try {
            InputStream inputStream = VertxConfig.class.getClassLoader().getResourceAsStream(file);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            return VertxConfig.Builder.build(reader);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    protected VertxConfig buildConfig(VertxConfig baseConfig, String file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalStateException("file can not be empty.");
        }

        VertxConfig result = baseConfig;
        Enumeration<URL> resources = VertxConfig.class.getClassLoader().getResources(file);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            BufferedReader reader = null;
            try {
                InputStream inputStream = resource.openStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                VertxConfig config = VertxConfig.Builder.build(reader);
                result.getRoutes().addAll(config.getRoutes());
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }

        return result;
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