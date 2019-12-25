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

import com.google.common.collect.Maps;
import org.joyqueue.toolkit.util.ASMUtils;
import com.jd.laf.web.vertx.Environment;
import com.jd.laf.web.vertx.RouteProvider;
import com.jd.laf.web.vertx.config.RouteConfig;
import com.jd.laf.web.vertx.config.VertxConfig;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Route;
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
 *
 * author: gaohaoxiang
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
    public void start() throws Exception {
        try {
            //构建配置数据
            config = inherit(buildConfig(buildBaseConfig(BASE_ROUTING), file));

            //初始化插件
            register(vertx, env, config);

            router = createRouter(env);
            //通过配置文件构建路由
            addRoutes(router, config.getRoutes(), env);
            //通过路由提供者构建路由
            if (providers != null) {
                for (RouteProvider provider : providers) {
                    addRoutes(router, provider.getRoutes(), env);
                }
            }
            //启动服务
            startHttpServer();
            //注册路由变更消息监听器
            dynamicRoute();
            logger.info(String.format("success starting routing verticle %d at %s", id, deploymentID()));
        } catch (Exception e) {
            logger.error(String.format("failed starting routing verticle %d at %s", id, deploymentID()), e);
            throw e;
        }
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