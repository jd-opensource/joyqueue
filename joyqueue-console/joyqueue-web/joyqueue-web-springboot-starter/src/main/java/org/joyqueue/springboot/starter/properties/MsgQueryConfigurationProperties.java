/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.springboot.starter.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * @author jiangnan53
 * @date 2020/4/21
 **/
@ConfigurationProperties(prefix = "message.query",ignoreUnknownFields = false)
public class MsgQueryConfigurationProperties {

    private boolean enabled = false;

    private final Environment environment;

    public MsgQueryConfigurationProperties(Environment environment) {
        Assert.notNull(environment, "Environment must not be null");
        this.environment = environment;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Environment getEnvironment() {
        return environment;
    }
}
