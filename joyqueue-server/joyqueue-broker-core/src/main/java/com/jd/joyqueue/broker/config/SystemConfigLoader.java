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
package com.jd.joyqueue.broker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * SystemConfigLoader
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/6/26
 */
public class SystemConfigLoader {

    protected static final Logger logger = LoggerFactory.getLogger(SystemConfigLoader.class);

    protected static final String SYSTEM_EVN_FILE = "system.properties";

    public static void load() {
        try {
            Properties sysEvn = new Properties();
            InputStream inputStream = SystemConfigLoader.class.getClassLoader().getResourceAsStream(SYSTEM_EVN_FILE);
            if (null != inputStream) {
                sysEvn.load(inputStream);
            }
            sysEvn.forEach((k, v) -> System.setProperty(k.toString(), v.toString()));
        } catch (IOException e) {
            logger.warn("load system config exception", e);
        }
    }
}