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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by chenyanying3 on 19-3-3.
 */
@SpringBootApplication
@PropertySource({"classpath:application.properties"})
@Import(H2DBServerAutoConfiguration.class)
public class WebApplication implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(WebApplication.class);
    @Value("${vertx.http.port}")
    private int port ;
    public static void main(String[] args) {
        System.setProperty("vertx.logger-delegate-factory-class-name","io.vertx.core.logging.SLF4JLogDelegateFactory");
        SpringApplication.run(WebApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("JoyQueue web started on port {}.", port);
    }
}
