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
package org.joyqueue.broker;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

/**
 * BannerPrinter
 *
 * author: gaohaoxiang
 * date: 2019/6/26
 */
public class BannerPrinter {

    protected static final Logger logger = LoggerFactory.getLogger(BannerPrinter.class);

    private static final String BANNER_RESOURCE = "joyqueue/banner";
    private static final String VERSION_RESOURCE = "joyqueue/version.properties";
    private static final String PLACEHOLDER_PREFIX = "{";
    private static final String PLACEHOLDER_SUFFIX = "}";

    public static void print() {
        InputStream inputStream = BannerPrinter.class.getClassLoader().getResourceAsStream(BANNER_RESOURCE);
        if (inputStream == null) {
            logger.warn("banner not exist, resource: {}", BANNER_RESOURCE);
            return;
        }
        try {
            String banner = IOUtils.toString(inputStream);
            Properties params = buildParams();
            banner = render(banner, params);
            print(System.out, banner);
        } catch (IOException e) {
            logger.warn("print banner exception", e);
        }
    }

    protected static Properties buildParams() {
        InputStream versionStream = BannerPrinter.class.getClassLoader().getResourceAsStream(VERSION_RESOURCE);
        Properties params = new Properties();

        try {
            if (versionStream != null) {
                params.load(versionStream);
            }
        } catch (IOException e) {
            logger.warn("load version exception", e);
        }
        return params;
    }

    protected static String render(String banner, Properties params) {
        for (String key : params.stringPropertyNames()) {
            banner = banner.replace(PLACEHOLDER_PREFIX + key + PLACEHOLDER_SUFFIX, params.getProperty(key));
        }
        return banner;
    }

    protected static void print(PrintStream stream, String banner) throws IOException {
        stream.write(banner.getBytes());
    }
}