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
package org.joyqueue.util;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * EnvironmentUtils
 * author: gaohaoxiang
 * date: 2019/11/8
 */
@Component
public class EnvironmentUtil implements EnvironmentAware {

    private static final String DEV = "dev";
    private static final String TEST = "test";
    private static final String PROD = "prod";

    private static Environment environment;

    public static boolean isDev() {
        return isCurrentEnv(DEV);
    }

    public static boolean isTest() {
        return isCurrentEnv(TEST);
    }

    public static boolean isProd() {
        return isCurrentEnv(PROD);
    }

    public static boolean isCurrentEnv(String env) {
        return ArrayUtils.contains(environment.getActiveProfiles(), env);
    }

    @Override
    public void setEnvironment(Environment environment) {
        EnvironmentUtil.environment = environment;
    }
}