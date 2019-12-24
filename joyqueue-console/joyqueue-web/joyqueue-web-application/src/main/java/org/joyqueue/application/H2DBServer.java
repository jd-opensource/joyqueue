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
package org.joyqueue.application;

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static org.h2.engine.Constants.DEFAULT_HTTP_PORT;
import static org.h2.engine.Constants.DEFAULT_TCP_PORT;

/**
 * Created by chenyanying3 on 19-4-13.
 */
public class H2DBServer {
    private static final Logger logger = LoggerFactory.getLogger(H2DBServer.class);

    public static final String BASE_DIR = "./h2-db-joyqueue";
    public static final int TCP_PORT = DEFAULT_TCP_PORT;
    public static final int WEB_PORT = DEFAULT_HTTP_PORT;

    private static Server tcpServer;
    private static Server webServer;

    public void init() {
        //server will be started while spring boot datasource connect h2
        //whether stared or not, defined by datasource auto configuration, not here
        if (tcpServer != null) {
            logger.info("h2 database tcp server is already started");
        }

        if (webServer != null) {
            logger.info("h2 database web server is already started.");
        }
    }

    public void start(String url) {
        try {
            String baseDir = getBaseDir(url);
            // tcp server 启动
            if (tcpServer == null) {
                logger.info("begin to create h2 database tcp server...");
                tcpServer = Server.createTcpServer(new String[]{"-tcp", "-tcpAllowOthers", "-tcpPort", getTcpPort(url), "-baseDir", baseDir}).start();
                logger.info(String.format("h2 database tcp server is started on port %s", TCP_PORT));
            } else if (!tcpServer.isRunning(true)) {
                logger.info("begin to start h2 database tcp server...");
                tcpServer.start();
                logger.info(String.format("h2 database tcp server is started on port %s", TCP_PORT));
            } else {
                logger.info("h2 database tcp server is already running");
            }
            // web console 启动
            if (webServer == null) {
                logger.info("begin to create h2 database web server...");
                webServer = Server.createWebServer(new String[]{"-web", "-webAllowOthers", "-webPort", String.valueOf(WEB_PORT), "-baseDir", baseDir}).start();
                logger.info(String.format("h2 database web server is started on port %s", WEB_PORT));
            } else if (!webServer.isRunning(true)) {
                logger.info("begin to start h2 database web server...");
                webServer.start();
                logger.info(String.format("h2 database web server is started on port %s", WEB_PORT));
            }else {
                logger.info("h2 database web server is already running.");
            }
        } catch (SQLException e) {
            logger.error("start h2 dabase server error", e);
        }
    }

    //jdbc:h2:tcp://127.0.0.1/./h2-db-joyqueue;INIT...
    protected String getBaseDir(String url) {
        try {
            String[] strs = url.split("//");
            return strs[1].substring(strs[1].indexOf("/")+1, strs[1].length()).split(";")[0];
        } catch (Exception e) {
//            logger.error("can not get base dir from connection url", e);
            return BASE_DIR;
        }
    }

    protected String getTcpPort(String url) {
        try {
            String[] strs = url.split("//");
            return strs[1].substring(0, strs[1].indexOf("/")).split(":")[1];
        } catch (Exception e) {
//            logger.error("can not get tcp port from connection url", e);
            return String.valueOf(DEFAULT_TCP_PORT);
        }
    }

    public void stop() {
        if (tcpServer != null && tcpServer.isRunning(true)) {
            tcpServer.stop();
            logger.info("h2 database tcp server is stopped.");
        }
        if (webServer != null && webServer.isRunning(true)) {
            webServer.stop();
            logger.info("h2 database web server is stopped.");
        }
    }

}
