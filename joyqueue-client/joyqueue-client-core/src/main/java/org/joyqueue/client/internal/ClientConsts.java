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
package org.joyqueue.client.internal;

import org.joyqueue.client.internal.transport.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * ClientConsts
 *
 * author: gaohaoxiang
 * date: 2019/3/5
 */
public class ClientConsts {

    protected static final Logger logger = LoggerFactory.getLogger(ClientConsts.class);

    private static final String VERSION_FILE = "META-INF/joyqueue/version.properties";

    public static final String PACKAGE_VERSION = Client.class.getPackage().getImplementationVersion();

    public static final String VERSION;

    static {
        InputStream versionInputStream = ClientConsts.class.getClassLoader().getResourceAsStream(VERSION_FILE);
        String version = PACKAGE_VERSION;

        try {
            Properties properties = new Properties();
            properties.load(versionInputStream);
            version = properties.getProperty("version", PACKAGE_VERSION);
        } catch (Exception e) {
            logger.warn("load version properties error", e);
        } finally {
            try {
                versionInputStream.close();
            } catch (Exception e) {
                logger.warn("close version properties error", e);
            }
        }

        VERSION = version;
    }
}