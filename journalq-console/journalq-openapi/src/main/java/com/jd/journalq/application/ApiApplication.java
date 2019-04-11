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
package com.jd.journalq.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import java.net.URL;

/**
 * Created by bjliuyong on 2018/11/30.
 */
@SpringBootApplication
@PropertySource({"classpath:important.properties"})
public class ApiApplication {

    public static void main(String[] args) {
        //设置日志
        URL resource = Thread.currentThread().getContextClassLoader().getResource("logging.properties");
        if (resource != null) {
            String path = resource.getFile();
            if (path != null) {
                System.setProperty("java.util.logging.config.file", path);
            }
        }
        System.setProperty("vertx.logger-delegate-factory-class-name","io.vertx.core.logging.SLF4JLogDelegateFactory");
        SpringApplication.run(ApiApplication.class, args);
    }
}
