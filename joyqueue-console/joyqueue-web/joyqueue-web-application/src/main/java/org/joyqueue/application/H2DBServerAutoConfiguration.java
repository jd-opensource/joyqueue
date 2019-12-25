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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by chenyanying3 on 19-4-13.
 */
@Configuration
@ConditionalOnProperty(name = "spring.datasource.driver", havingValue = "h2")
public class H2DBServerAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(H2DBServerAutoConfiguration.class);

    @Bean(value = "h2DBServer", initMethod = "init", destroyMethod = "stop")
    public H2DBServer runH2DBServer() {
        return new H2DBServer();
    }

}
