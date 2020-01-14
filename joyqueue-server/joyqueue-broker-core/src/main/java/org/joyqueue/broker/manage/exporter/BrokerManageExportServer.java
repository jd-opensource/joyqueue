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
package org.joyqueue.broker.manage.exporter;

import com.google.common.collect.Maps;
import org.joyqueue.broker.manage.config.BrokerManageConfig;
import org.joyqueue.broker.manage.exporter.vertx.RoutingVerticle;
import org.joyqueue.toolkit.service.Service;
import com.jd.laf.web.vertx.Environment;
import io.vertx.core.http.HttpServerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * BrokerManageExportServer
 *
 * author: gaohaoxiang
 * date: 2018/10/16
 */
public class BrokerManageExportServer extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(BrokerManageExportServer.class);

    private BrokerManageConfig config;
    private RoutingVerticle routingVerticle;
    private Map<String, Object> serviceMap = Maps.newHashMap();

    public BrokerManageExportServer(BrokerManageConfig config) {
        this.config = config;
    }

    public void registerServices(Map<String, Object> serviceMap) {
        this.serviceMap.putAll(serviceMap);
    }

    public void registerService(String key, Object service) {
        serviceMap.put(key, service);
    }

    protected RoutingVerticle initRoutingVerticle(BrokerManageConfig config) {
        HttpServerOptions httpServerOptions = new HttpServerOptions();
        httpServerOptions.setPort(config.getExportPort());
        return new RoutingVerticle(new Environment.MapEnvironment(), httpServerOptions);
    }

    @Override
    protected void validate() throws Exception {
        this.routingVerticle = initRoutingVerticle(config);
    }

    @Override
    protected void doStart() throws Exception {
        try {
            routingVerticle.registerServices(serviceMap);
            routingVerticle.start();
            logger.info("broker manage server is started, port: {}", config.getExportPort());
        } catch (Exception e) {
            logger.error("broker manage server start exception", e);
            throw e;
        }
    }

    @Override
    protected void doStop() {
        try {
            routingVerticle.stop();
        } catch (Exception e) {
            logger.error("broker manage server stop exception", e);
        }
    }
}